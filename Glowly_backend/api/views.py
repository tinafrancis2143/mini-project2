from rest_framework import generics
from django.contrib.auth.models import User
from .serializers import UserSerializer
from rest_framework.permissions import AllowAny

# This view uses the UserSerializer to handle creating a new user.
class CreateUserView(generics.CreateAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    # This permission allows any user (even unauthenticated ones) to access this view.
    permission_classes = [AllowAny]