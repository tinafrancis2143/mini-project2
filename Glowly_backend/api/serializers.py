from django.contrib.auth.models import User
from rest_framework import serializers
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer

# This serializer is for CREATING a new user (Signup)
class UserSerializer(serializers.ModelSerializer):
    password2 = serializers.CharField(style={'input_type': 'password'}, write_only=True)
    full_name = serializers.CharField(source='first_name', write_only=True)
    
    class Meta:
        model = User
        fields = ('id', 'username', 'email', 'full_name', 'password', 'password2')
        extra_kwargs = {
            'password': {'write_only': True},
            'username': {'read_only': True}
        }

    def validate(self, data):
        if data['password'] != data['password2']:
            raise serializers.ValidationError({"password": "Passwords do not match."})
        return data

    def create(self, validated_data):
        validated_data.pop('password2')
        full_name_data = validated_data.get('first_name')
        
        user = User.objects.create_user(
            username=validated_data['email'],
            email=validated_data['email'],
            first_name=full_name_data,
            password=validated_data['password']
        )
        return user

# This serializer is for CHECKING a user's credentials (Login)
class MyTokenObtainPairSerializer(TokenObtainPairSerializer):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        # We change the 'username' field to accept an 'email'
        self.fields['username'] = serializers.EmailField()

    @classmethod
    def get_token(cls, user):
        token = super().get_token(user)
        # Add custom claims
        token['username'] = user.username
        return token