package com.example.ktorchat.utils

object Constants {

    // SCREENS
    const val CHAT_ROOMS = "chat rooms"
    const val CHAT_SCREEN = "chat_screen"
    const val USERNAME_SCREEN = "username_screen"


    // CONNECTION STATUS
    const val CONNECTION_GOOD = 1
    const val CONNECTION_LOST = 2
    const val CONNECTION_HOLD = 3
    const val CONNECTION_INITIATE = 4


    // AUTH OPTIONS
    const val AUTH_SIGN_UP = 0
    const val AUTH_SIGN_IN = 1

    // AUTH PREF
    const val AUTH_SHARED_PREF = "Auth shared pref"
    const val AUTH_TOKEN = "auth token"
    const val USER_ID = "user id"


    // ENCRYPTED USER INFO
    const val USER_NAME = "user name"
    const val USER_PASSWORD = "user password"


    //MESSAGE PAGINATION
    const val PAGE_SIZE = 8

    //MESSAGE STATUS
    const val MESSAGE_STATUS_UNSENT = 0
    const val MESSAGE_STATUS_NOT_READ = 1
    const val MESSAGE_STATUS_READ = 2

    //Pending messages
    const val PENDING_MESSAGES = "pending messages"

    // Pending Profile Updates
    const val PENDING_PROFILE_PIC_UPDATE = "pending profile updates"



    //TOOLBAR NAV COMMANDS

    const val TOOLBAR_NAV_TO_PROFILE_SCREEN = 0
    const val TOOLBAR_NAV_FROM_CHAT_SCREEN = 1

    // VOICE CALL STATUS

    const val VOICE_CALL_DECLINE = 0
    const val VOICE_CALL_ACCEPT = 1
    const val VOICE_CALL_REQUEST = 2

}

