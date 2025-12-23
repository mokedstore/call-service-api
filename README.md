# Call Service
A restful service responsible for handling alarms originating from G1. The service fetches alarms, acknowledge them, get alarms content, converts text content to speech
and start call with alarm contacts until getting an answer or until there are no more contacts left. This service is responsible for saving each alarm and conversation state on SQL database and to update G1 systems for any relevant event during the alarm handling. This service uses Microsoft text to speech services and Vonage services for handling calls.

## Before Deployment
Before deploying Call Service verify the following exists:
- network folder for storing configuration.
- network folder for storing dynamic and static audio files
- sql database with the following table:
*Alerts* - for storing alarms state:
```sql
USE [Call_Automation_Dev]
GO

/****** Object:  Table [dbo].[Alerts]    Script Date: 12/9/2025 3:00:34 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Alerts](
	[kId] [nvarchar](40) NOT NULL,
	[createdAt] [datetime2](7) NOT NULL,
	[updatedAt] [datetime2](7) NULL,
	[siteNumber] [nvarchar](40) NOT NULL,
	[systemNumber] [nvarchar](40) NOT NULL,
	[alarmIncidentNumber] [nvarchar](40) NOT NULL,
	[dispatchLocation] [nvarchar](10) NULL,
	[alarmEventId] [nvarchar](40) NOT NULL,
	[currentWriteEventCode] [nvarchar](10) NULL,
	[fullClearStatus] [nvarchar](5) NULL,
	[isActiveAlert] [bit] NOT NULL,
	[alertHandlingStatusCode] [nvarchar](5) NULL,
	[alertHandlingStatusMessage] [nvarchar](255) NULL,
	[progressMessages] [nvarchar](max) NULL,
	[contacts] [nvarchar](max) NULL,
	[callGeneratedText] [nvarchar](max) NULL,
	[textToSpeechFileLocation] [nvarchar](500) NULL,
	[vonageCurrentConversationId] [nvarchar](45) NULL,
	[answeredPhoneNumber] [nvarchar](25) NULL,
	[orderOfAnsweredCall] [int] NULL,
	[vonageConversationLength] [int] NULL,
	[customerResponseToCall] [nvarchar](50) NULL,
	[alertDate] [datetime2](7) NULL,
	[alertZoneId] [nvarchar](10) NULL,
	[csNumber] [nvarchar](40) NULL,
PRIMARY KEY CLUSTERED 
(
	[kId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

ALTER TABLE [dbo].[Alerts] ADD  DEFAULT (sysdatetime()) FOR [alertDate]
GO
```
*Conversations* - for keeping conversation events from Vonage API:
```sql
USE [Call_Automation_Dev]
GO

/****** Object:  Table [dbo].[Conversations]    Script Date: 12/9/2025 3:01:44 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[Conversations](
	[conversationId] [nvarchar](45) NOT NULL,
	[uuid] [nvarchar](45) NOT NULL,
	[fromNo] [nvarchar](25) NOT NULL,
	[toNo] [nvarchar](25) NOT NULL,
	[eventTimestamp] [datetime2](7) NOT NULL,
	[disconnectedBy] [nvarchar](50) NULL,
	[duration] [int] NULL,
	[rate] [decimal](18, 8) NULL,
	[price] [decimal](18, 8) NULL,
	[startTime] [datetime2](7) NULL,
	[endTime] [datetime2](7) NULL,
	[rawEvent] [nvarchar](max) NULL,
	[kId] [nvarchar](40) NOT NULL,
	[STATUS] [nvarchar](50) NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
```

*DispatchIdToPhoneNumber* - get dispatch phone number from dispatch location
```sql
USE [Call_Automation_Dev]
GO

/****** Object:  Table [dbo].[DispatchIdToPhoneNumber]    Script Date: 12/9/2025 3:03:38 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[DispatchIdToPhoneNumber](
	[dispatchLocation] [nvarchar](10) NOT NULL,
	[dispatchSiteName] [nvarchar](60) NOT NULL,
	[dispatchPhoneNumber] [nvarchar](20) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[dispatchLocation] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
```

*TextToSpeechMessages* - gets message id from alarm event id
```sql
USE [Call_Automation_Dev]
GO

/****** Object:  Table [dbo].[TextToSpeechMessages]    Script Date: 12/9/2025 3:04:50 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[TextToSpeechMessages](
	[eventId] [nvarchar](30) NOT NULL,
	[messageId] [nvarchar](30) NULL,
PRIMARY KEY CLUSTERED 
(
	[eventId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
```
- connection to Azure Text to Speech Service and valid APIKey
- Connection to Vonage API and valid application credentials + a gateway to allow Vonage to reach Call Service with HTTP calls.

## Build & Deployment
- Prequisites:
    - Java 11
    - Apache Maven 3.6+
    - Apache tomcat 10+

- **Environment Variables**
- *IS_ALERT_FETCHER* - flag indicating if this instance is responsible for collecting alarms from G1 API. We can have many instances of *CallService* for high availability but only one instance can be responible for fetching alerts. Valid values: true, false.
- *CALL_SERVICE_LOG_LEVEL* - log level of Call Service (logs are located in Tomcat installation folder: C:\Program Files\Apache Software Foundation\Tomcat 10.1\logs). valid values: error, warning, info, debug, trace.
- *CALL_SERVICE_CONFIGURATION_DIR_PATH* - path to folder containing configurations.json file with configurations of this service.
- *CALL_SERVICE_CONFIGURATION_FILE_PATH* - path to file containing configurations.json file with configurations of this service (CALL_SERVICE_CONFIGURATION_DIR_PATH/configurations.json).

- **configurations.json**
```json
{
  "sql": {
    "username": "kpmp_db_user",
    "url": "jdbc:sqlserver://x.x.x.x:1433;databaseName=Call_Automation_Dev;encrypt=false;",
    "password": "xyz",
    "connectionProperties": "initializeSize=3;maxTotal=20;maxIdle=10"
  },
  "vonage": {
    "applicationId": "123456-4847-2233-ac65-92a658ae1234",
    "privateKeyFilePath": "C:\\Users\\Automation\\vonage\\private.key",
    "jwtValidInMinutes": 25,
    "callsUrl": "https://api.nexmo.com/v1/calls",
    "answerUrlEndpoint": "http://automation.g1-group.com:8000/CallService/api/vonage/answer",
    "streamUrlEndpoint": "http://automation.g1-group.com:8000/CallService/api/vonage/start/call/audio/mp3",
    "eventUrlEndpoint": "http://automation.g1-group.com:8000/CallService/api/vonage/event",
    "streamUrlStaticEndpoint": "http://automation.g1-group.com:8000/CallService/api/vonage/get/static/audio/mp3",
    "staticFilesNames": {
      "noInput": "noInputReceived.mp3",
      "goodbye": "goodbye.wav",
      "transfer": "transfer.wav",
      "invalidOption": "invalidOption.mp3"
    },
    "maxRingTimeSeconds": 25,
    "transferCallRingTimeout": 15,
    "clientResponseTimeoutSeconds": 7
  },
  "maxTimeOpenedAlertMinutes": 30,
  "considerConversationAsApprovedIfLongerThanInSeconds": 15,
  "dynamicSpeechFilesLocation": "\\\\G4S-FS\\Automation-REC\\dynamicAudioFiles",
  "staticSpeechFilesLocation": "\\\\G4S-FS\\Automation-REC\\staticAudioFiles",
  "azureTextToSpeech": {
    "key": "xxxxxxxxxyyyyyyyyyzzzzzzzzzz",
    "url": "https://westeurope.tts.speech.microsoft.com/cognitiveservices/v1",
    "outuputFormat": "audio-16khz-32kbitrate-mono-mp3"
  },
  "mainContactNumberOfTries": 2,
  "otherContactsNumberOfTries": 1,
  "waitTimeBetweenGetOpenAlertsCallsInMilliseconds": 15000,
  "waitTimeBetweenCheckAlertOpenForTooLongInMilliseconds": 600000,
  "clearConversationCacheIntervalMilliseconds": 3600000,
  "defaultDispatchPhoneNumber": "97239374584",
  "errorNotificationPhoneNumber": "0529595811",
  "errorNotificationMessage": "בדיקות בדיקות: תקלה בפעילות הרובוטים, יש לבדוק את תקינות המערכת ולהפעיל נוהל תקלה במידת הצורך",
  "errorNotificationSubject": "בדיקות: תקלה בפעילות הרובוט",
  "maxResponsesPerConversation": 5,
  "g1ServicesBaseUrl": "http://172.16.1.206:8000",
  "callServiceBaseUrl": "http://localhost:8000",
  "g1EventsApiEmpNo": 16,
  "g1EventsApiUserName": "Nexus"
}
```

- **sql.username** – SQL user for database authentication.
- **sql.url** – JDBC connection string used to connect to the SQL Server instance.
- **sql.password** – Password for the SQL authentication user.
- **sql.connectionProperties** – Additional JDBC/connection-pool properties.

- **vonage.applicationId** – Vonage application ID used for API authentication.
- **vonage.privateKeyFilePath** – Path to the private key file used for generating JWTs and authenticate against Vonage.
- **vonage.jwtValidInMinutes** – JWT validity period in minutes.
- **vonage.callsUrl** – Vonage API endpoint used to initiate calls.
- **vonage.answerUrlEndpoint** – Webhook endpoint invoked when a call is answered (Gateway on G1 side).
- **vonage.streamUrlEndpoint** – Endpoint to start streaming audio to an active call (Gateway on G1 side).
- **vonage.eventUrlEndpoint** – Webhook endpoint receiving call events (Gateway on G1 side).
- **vonage.streamUrlStaticEndpoint** – Endpoint for serving static audio files to calls (Gateway on G1 side).

- **vonage.staticFilesNames.noInput** – Audio file location for "no input received".
- **vonage.staticFilesNames.goodbye** – Audio file location for "goodbye" message.
- **vonage.staticFilesNames.transfer** – Audio file location for "transfer" message.
- **vonage.staticFilesNames.invalidOption** – Audio location file for "invalid option" message.

- **vonage.maxRingTimeSeconds** – Maximum ringing time before timeout.
- **vonage.transferCallRingTimeout** – Ring timeout for call transfers.
- **vonage.clientResponseTimeoutSeconds** – Time to wait for caller input.

- **maxTimeOpenedAlertMinutes** – Maximum allowed minutes for an open alert before decalred open for too long and send error.
- **considerConversationAsApprovedIfLongerThanInSeconds** – Minimum duration considered as approved conversation even if not getting response from the client.

- **dynamicSpeechFilesLocation** – Path to dynamically generated speech files.
- **staticSpeechFilesLocation** – Path to static prerecorded speech files.

- **azureTextToSpeech.key** – Azure TTS API key.
- **azureTextToSpeech.url** – Azure TTS service URL.
- **azureTextToSpeech.outuputFormat** – Requested audio output format.

- **mainContactNumberOfTries** – Number of attempts to call the main contact before moving to next contact.
- **otherContactsNumberOfTries** – Number of attempts to call to other contacts before moving to next contact.

- **waitTimeBetweenGetOpenAlertsCallsInMilliseconds** – Interval for polling open alerts.
- **waitTimeBetweenCheckAlertOpenForTooLongInMilliseconds** – Interval for checking overdue alerts.
- **clearConversationCacheIntervalMilliseconds** – Cache cleanup interval for conversations.

- **defaultDispatchPhoneNumber** – Default phone number used for dispatching when transferring calls.
- **errorNotificationPhoneNumber** – Number receiving error notifications.
- **errorNotificationMessage** – Message sent on system errors.
- **errorNotificationSubject** – Subject for error notifications.

- **maxResponsesPerConversation** – Maximum responses from contacts allowed in one conversation before hanging the call.

- **g1ServicesBaseUrl** – Base URL for G1 service APIs.
- **callServiceBaseUrl** – Base URL for internal call-service API.

- **g1EventsApiEmpNo** – Employee number for G1 Events API.
- **g1EventsApiUserName** – Username for G1 Events API.

**Deployment**
1) Navigate to root folder of this project (where pom.xml is located).
2) Run `mvn clean install package` - this should create CallService.war file under the target folder in the project folder.
3) Copy CallService.war from target folder to Apache Tomcat webapps folder (if war file already exists it is recommended to stop Tomcat, delete all relevant resources, copy new war file and start Tomcat).

## API

**For all web methods, if error occurs (status is different than 2XX) then Response body is as follows:**
```json
{
    "error": "general error code e.g NOT_FOUND",
    "message": "specific error msesage"
}
```

#### Check service is running
Endpoint to validate Service is up and running

`GET /CallService/api/action/ping`
###### Parameters
- None
###### Body
- None

###### Response Status
- <b>200</b> - Success status
###### Response Body
```json
{
	"status": "up and running"
}
```

#### Manually Handle Alerts Open For Too Long
Handling of alerts open for too long runs by default every 10 minutes. You can run long alarm handling call manually with this endpoint

`GET /CallService/api/action/manually/handle/alerts/open/for/too/long`
###### Parameters
- None
###### Body
- None

###### Response Status
- <b>200</b> - Success status
###### Response Body
```json
{
	"status": "sent request to manually handle alerts open for too long"
}
```

#### Stop Alert Fetching
Stops fetching alerts from G1 /api/alarms endpoint. this is useful before service restart as it allows to handle all open alarms before shutting service down.
`POST /CallService/api/action/stop/alert/fetching`
###### Parameters
- None
###### Body
- None

- <b>201</b> - Created status
###### Response Body
```json
{
    "message": "stopped alerts fetching"
}
```

#### Start Alert Fetching
Starts fetching alerts from G1 /api/alarms endpoint if alert fetching is stopped.
`POST /CallService/api/action/start/alert/fetching`
###### Parameters
- None
###### Body
- None

- <b>201</b> - Created status
###### Response Body
```json
{
    "message": "started alerts fetching"
}
```

#### Text to Speech (Internal Use)
converts text content in ssml format to audio file and saves it locally. **This function is used by Call Service itself and should not be used by clients**
`POST /CallService/api/action/text/to/speech`
###### Parameters
- None
###### Body
```json
{
    "ssml": "...."
}
```
-*ssml* - text content in SSML format to convert to audio file

- <b>201</b> - Created status
###### Response Body
```json
{
    "path": "/path/to/file"
}
```
- path to created file
###### Errors
- **400** - Bad Request - in case request body is not valid (bad JSON, malformed email address, not postive priority).
- **500** - Internal service error.

*The service includes more RESTFUL endpoints for intetnal usage which should not be used by clients. e.g. endpoints for vonage to send webhooks and endpoints to get data from SQL*