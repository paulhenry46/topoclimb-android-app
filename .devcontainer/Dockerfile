FROM mcr.microsoft.com/devcontainers/base:ubuntu

# Install dependencies
RUN apt-get update && apt-get install -y \
    unzip curl zip lib32stdc++6 lib32z1

# Install Android SDK command line tools
RUN mkdir -p /home/vscode/android-sdk/cmdline-tools && \
    cd /home/vscode/android-sdk/cmdline-tools && \
    curl -O https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip && \
    unzip commandlinetools-linux-9477386_latest.zip && \
    mv cmdline-tools latest

# Set environment variables
ENV ANDROID_HOME=/home/vscode/android-sdk
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools