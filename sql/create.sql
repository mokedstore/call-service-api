-- CREATE TABLE Generic.dbo.Conversations (
--     conversationId  NVARCHAR(45)       NOT NULL, -- מזהה שיחה מ-Vonage
--     uuid            NVARCHAR(45)       NOT NULL, -- מזהה session ב-Vonage
--     fromNo          NVARCHAR(25)        NOT NULL, -- מספר הטלפון אליו התקשרו
--     toNo            NVARCHAR(25)        NOT NULL, -- מספר הטלפון ממנו יצאה השיחה
--     eventTimestamp     DATETIME2        NOT NULL, -- מועד הזמן בו התקבל האירוע (UTC)
--     disconnectedBy  NVARCHAR(50)       NULL,     -- הגורם שניתק את השיחה
--     duration        INT                 NULL,     -- משך השיחה בשניות
--     rate            DECIMAL(18,8)       NULL,     -- תעריף לדקה ביורו
--     price           DECIMAL(18,8)       NULL,     -- עלות השיחה ביורו
--     startTime       DATETIME2           NULL,     -- מועד תחילת השיחה
--     endTime         DATETIME2           NULL,     -- מועד סיום השיחה
--     rawEvent        NVARCHAR(MAX)       NULL      -- האירוע הגולמי בפורמט JSON
-- );

CREATE TABLE Generic.dbo.TextToSpeechMessages (
    eventId NVARCHAR(30) PRIMARY KEY,
    messageId NVARCHAR(30)
);


-- USE master;
-- ALTER DATABASE Generic SET SINGLE_USER WITH ROLLBACK IMMEDIATE;

-- ALTER DATABASE Generic COLLATE Hebrew_CI_AS;

-- ALTER DATABASE Generic SET MULTI_USER;

-- ALTER DATABASE Generic 
-- COLLATE Hebrew_CI_AS;

DELETE FROM Generic.dbo.TextToSpeechMessages;

SELECT * FROM Generic.dbo.TextToSpeechMessages;

INSERT INTO  Generic.dbo.TextToSpeechMessages VALUES('130', N'1.פתיח');
INSERT INTO  Generic.dbo.TextToSpeechMessages VALUES(N'noAnswer', N'אין מענה');

CREATE TABLE Generic.dbo.Alerts (
    kId                        NVARCHAR(40)     NOT NULL PRIMARY KEY, -- מזהה חד ערכי (UUID) במערכת
    createdAt                   DATETIME2        NOT NULL, -- מועד יצירת הרשומה (UTC)
    updatedAt                   DATETIME2        NULL,     -- מועד עדכון אחרון של הרשומה (UTC)
    siteNumber                  NVARCHAR(40)    NOT NULL, -- מזהה חד ערכי של האתר
    systemNumber                NVARCHAR(40)    NOT NULL, -- מזהה חד ערכי של המערכת
    alarmIncidentNumber         NVARCHAR(40)    NOT NULL, -- מזהה התראה כפי שהגיע מ-API של G1
    dispatchLocation            NVARCHAR(10)    NULL,     -- מזהה מיקום מוקד
    alarmEventId               NVARCHAR(40)    NOT NULL, -- מזהה סוג ההתראה
    currentWriteEventCode       NVARCHAR(10)     NULL,     -- הערך האחרון שנשלח לממשק Write event
    fullClearStatus             NVARCHAR(5)      NULL,     -- N או Y
    isActiveAlert               BIT              NOT NULL, -- האם ההתראה פעילה
    alertHandlingStatusCode     NVARCHAR(5)     NULL,     -- סטטוס הטיפול בהתראה
    alertHandlingStatusMessage  NVARCHAR(255)    NULL,     -- מלל קצר לסטטוס
    progressMessages            NVARCHAR(MAX)    NULL,     -- JSON עם היסטוריית ההתראה
    contacts                    NVARCHAR(MAX)    NULL,     -- JSON עם מספרי אנשי קשר
    callGeneratedText           NVARCHAR(MAX)    NULL,     -- מלל השיחה בפורמט SSML
    textToSpeechFileLocation    NVARCHAR(500)    NULL,     -- נתיב לקובץ WAV
    vonageCurrentConversationId NVARCHAR(45)    NULL,     -- מזהה שיחה מול Vonage
    answeredPhoneNumber         NVARCHAR(25)     NULL,     -- מספר הטלפון שענה
    orderOfAnsweredCall         INT              NULL,     -- המיקום במערך אנשי קשר
    vonageConversationLength    INT              NULL,     -- משך השיחה בשניות
    customerResponseToCall      NVARCHAR(50)    NULL,      -- הערך שהלקוח הזין
    alertDate                   DATETIME2        NOT NULL
);

-- INSERT INTO Generic.dbo.Alerts (
--     kId,
--     createdAt,
--     updatedAt,
--     siteNumber,
--     systemNumber,
--     alarmIncidentNumber,
--     dispatchLocation,
--     alaramEventId,
--     currentWriteEventCode,
--     fullClearStatus,
--     isActiveAlert,
--     alertHandlingStatusCode,
--     alertHandlingStatusMessage,
--     progressMessages,
--     contacts,
--     callGeneratedText,
--     textToSpeechFileLocation,
--     vonageCurrentConversationId,
--     answeredPhoneNumber,
--     orderOfAnsweredCall,
--     vonageConversationLength,
--     customerResponseToCall
-- ) VALUES (
--     N'ALERT-1234567890',
--     '2025-08-11T10:15:00Z',
--     '2025-08-11T11:00:00Z',
--     N'SITE-001',
--     N'SYS-045',
--     N'INC-20250811-0001',
--     N'DIS-12',
--     N'EVT-ALARM-001',
--     N'CODE123',
--     N'Y',
--     1,
--     N'12',
--     N'בטיפול מוקד',
--     N'[{"timestamp":"2025-08-11T10:15:00Z","level":"info","message":"התראה נפתחה"},{"timestamp":"2025-08-11T10:20:00Z","level":"warning","message":"מוקד הוזעק"}]',
--     N'["+972501234567","+972548765432"]',
--     N'<speak>שלום, התקבלה התראה חדשה.</speak>',
--     N'\\networkshare\tts\alert001.wav',
--     N'VONAGE-CONV-9999',
--     N'+972501234567',
--     1,
--     180,
--     N'1'
-- );

-- INSERT INTO Generic.dbo.Conversations (
--     conversationId,
--     uuid,
--     fromNo,
--     toNo,
--     eventTimestamp,
--     disconnectedBy,
--     duration,
--     rate,
--     price,
--     startTime,
--     endTime,
--     rawEvent
-- ) VALUES (
--     N'VONAGE-CONV-9999',
--     N'VONAGE-UUID-1234',
--     N'+972501234567',
--     N'+972548765432',
--     '2025-08-11T10:16:00Z',
--     N'user',
--     180,
--     0.02610000,
--     0.07830000,
--     '2025-08-11T10:16:00Z',
--     '2025-08-11T10:19:00Z',
--     N'{"event":"conversation:ended","reason":"completed"}'
-- );

ALTER TABLE Generic.dbo.Conversations
ADD kId NVARCHAR(40) NULL;

UPDATE Generic.dbo.Conversations
SET kId = 'default_kid_value' -- replace with actual default or generated value
WHERE kId IS NULL;

ALTER TABLE Generic.dbo.Conversations
ALTER COLUMN kId NVARCHAR(40) NOT NULL;

CREATE TABLE Generic.dbo.DispathIdToPhoneNumber (
    dispatchLocation            NVARCHAR(10) NOT NULL PRIMARY KEY,
    dispatchSiteName            NVARCHAR(60)  NOT NULL,
    dispatchPhoneNumber         NVARCHAR(20)    NOT NULL
);

INSERT INTO Generic.dbo.DispathIdToPhoneNumber (dispatchLocation, dispatchSiteName, dispatchPhoneNumber) VALUES
(N'100', N'ארצי VIP', N'97239374564'),
(N'101', N'מוקד עסקי', N'97239374584'),
(N'102', N'ארצי עסקיים', N'97239374564'),
(N'103', N'מוסדי', N'97239374584'),
(N'104', N'פרטי', N'97239374564'),
(N'105', N'מוקד רואה', N'97239374542'),
(N'1055', N'טעוני רישוי מוקד רואה', N'97239374542'),
(N'107', N'מוקד רואה ארצי', N'97239374542'),
(N'109', N'פרטי', N'97239374564'),
(N'110', N'מוקד עסקי 2', N'97239374584'),
(N'111', N'מוקד מטר', N'97239374584'),
(N'113', N'מוקד שחר', N'97289255355'),
(N'114', N'טעוני רישוי באר שבע', N'97239374584'),
(N'116', N'מוקד רואה שחר', N'97289255355'),
(N'118', N'אנליטיקה שחר', N'97289255355'),
(N'119', N'מוקד כשר', N'97239374542'),
(N'122', N'אנליטיקה פרו', N'97239374542'),
(N'151', N'ארצי רובוט', N'97239374564'),
(N'152', N'משרד החינוך', N'97239374584'),
(N'153', N'כלבי אשמורת', N'97239374564'),
(N'155', N'IWATCHER', N'97239374564'),
(N'200', N'טעוני רישוי שחר', N'97289255355'),
(N'99', N'ארצי', N'97239374564'),
(N'666', N'מוקד רואה אנליטיקה', N'97239374542');