# SyncroEdit
Collaborative note editing app with firebase as BaaS

## Introduction
This is a simple note taking android application that uses firebase as backend. It lets users create notes and share with people on user's contacts. I created this as a sample app for learning firebase and it's features. The following firebase features are used:

- Realtime database (For storing user and notes data)
- Storage (Only images)
- Authentication (Only google login as of now)
- Remote Config (for changing app suff on the fly)
- Test lab (For Robo and instrumentation tests)
- Crash reporting
- Cloud messaging (for sharing note info with collaborators)

## How to use
Directly cloning this repo will not work as I have omitted some mandatory files from the repo. Here are the steps on how to make this codebase work and get relevant firebase files:

- Clone the repo and change package name (change from org.drulabs.syncroedit to say com.sample.notecollab)
- Log on to https://firebase.google.com/
- Create a new project in firebase console with your new package (com.sample.notecollab).
- Follow the instructions and add your computer's SHA1 fingerprint.
- download ***google-services.json*** when prompted and put it in app folder.
- go to settings and copy the API key and replace the API_KEY in Constants.java class under config package.


In the first screen, there is no phone number verification, however this number is used for mapping FCM token and other app stuff. Make sure this number is right as this is the unique identifier of a user

### Got any queries or suggestions
- drop me a mail heavenly.devil09@gmail.com
- checkout my blog: https://androiddevsimplified.wordpress.com/
- follow me on twitter: https://twitter.com/drulabs
