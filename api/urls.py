from django.urls import path
from .views import CreateUserView
from rest_framework_simplejwt.views import TokenObtainPairView

urlpatterns = [
    # This URL is for creating a new user. It uses the view you just wrote.
    path('signup/', CreateUserView.as_view(), name='signup'),
    # This URL is for logging in. It uses a pre-built view from the library we installed.
    path('login/', TokenObtainPairView.as_view(), name='login'),
]