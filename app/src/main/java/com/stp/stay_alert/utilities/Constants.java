package com.stp.stay_alert.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_ADMIN = "admin";
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_COLLECTION_REPORTS = "admin_reports";
    public static final String KEY_DATE = "date";
    public static final String KEY_INCIDENT = "incident_description";
    public static final String KEY_TIME_DEPARTURE_FROM_THE_OFFICE = "time_departure_from_office";
    public static final String KEY_TIME_DEPARTURE_FROM_THE_INCIDENT = "time_departure_from_incident";
    public static final String KEY_TIME_ARRIVAL_TO_THE_INCIDENT = "time_arrival_to_the_incident";
    public static final String KEY_TIME_ARRIVAL_TO_THE_OFFICE = "time_arrival_to_the_office";
    public static final String KEY_AGE = "age";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_CONDITION_OF_PATIENT = "condition_of_patient(s)";
    public static final String KEY_TREATMENT_APPLIED = "treatment_applied";
    public static final String KEY_CONTACT_PERSON = "contact_person";
    public static final String KEY_CONTACT_NUMBER = "contact_number";
    public static final String KEY_RELATIONSHIP = "relationship";
    public static final String KEY_LOCATION_OF_INCIDENT = "location_of_the_incident";
    public static final String KEY_RESPONDERS = "responders";
    public static final String KEY_DRIVER = "driver";
    public static final String KEY_REFERRED_TO = "referred_to";
    public static final String KEY_VEHICLE_USED = "vehicle_used";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_CONTACT = "contact";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_FULL_NAME = "full_name";
    public static final String KEY_PREFERENCE_NAME = "stay-alert";
    public static final String KEY_IS_SIGN_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_USER_TYPE = "user_type";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static final String KEY_LOC_LATITUDE = "latitude";
    public static final String KEY_LOC_LONGITUDE = "longitude";
    public static final String KEY_MEDIA_PATH = "media_path";
    public static final String KEY_MSG_TYPE = "msg_type";
    public static final  String KEY_INCIDENT_ID = "incident_id";


    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders(){
        if (remoteMsgHeaders == null){
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAhb1SiZo:APA91bGUiV_SE_6P83vSEZEk34pwzIOV5bjY7SwCtnchsNDcbsHyEnBxlc2V4c4E83uBAMJNsjlm7i_EdUGP9u2Va7kL21yi_mzkISIoIi1QelqFa-ajmicrz6nJWkWXOW7L3gB2GRYK"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }


}
