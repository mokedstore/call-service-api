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

-- CREATE TABLE Generic.dbo.Alerts (
--     kId                        NVARCHAR(40)     NOT NULL PRIMARY KEY, -- מזהה חד ערכי (UUID) במערכת
--     createdAt                   DATETIME2        NOT NULL, -- מועד יצירת הרשומה (UTC)
--     updatedAt                   DATETIME2        NULL,     -- מועד עדכון אחרון של הרשומה (UTC)
--     siteNumber                  NVARCHAR(40)    NOT NULL, -- מזהה חד ערכי של האתר
--     systemNumber                NVARCHAR(40)    NOT NULL, -- מזהה חד ערכי של המערכת
--     alarmIncidentNumber         NVARCHAR(40)    NOT NULL, -- מזהה התראה כפי שהגיע מ-API של G1
--     dispatchLocation            NVARCHAR(10)    NULL,     -- מזהה מיקום מוקד
--     alaramEventId               NVARCHAR(40)    NOT NULL, -- מזהה סוג ההתראה
--     currentWriteEventCode       NVARCHAR(10)     NULL,     -- הערך האחרון שנשלח לממשק Write event
--     fullClearStatus             NVARCHAR(5)      NULL,     -- N או Y
--     isActiveAlert               BIT              NOT NULL, -- האם ההתראה פעילה
--     alertHandlingStatusCode     NVARCHAR(5)     NULL,     -- סטטוס הטיפול בהתראה
--     alertHandlingStatusMessage  NVARCHAR(255)    NULL,     -- מלל קצר לסטטוס
--     progressMessages            NVARCHAR(MAX)    NULL,     -- JSON עם היסטוריית ההתראה
--     contacts                    NVARCHAR(MAX)    NULL,     -- JSON עם מספרי אנשי קשר
--     callGeneratedText           NVARCHAR(MAX)    NULL,     -- מלל השיחה בפורמט SSML
--     textToSpeechFileLocation    NVARCHAR(500)    NULL,     -- נתיב לקובץ WAV
--     vonageCurrentConversationId NVARCHAR(45)    NULL,     -- מזהה שיחה מול Vonage
--     answeredPhoneNumber         NVARCHAR(25)     NULL,     -- מספר הטלפון שענה
--     orderOfAnsweredCall         INT              NULL,     -- המיקום במערך אנשי קשר
--     vonageConversationLength    INT              NULL,     -- משך השיחה בשניות
--     customerResponseToCall      NVARCHAR(50)    NULL      -- הערך שהלקוח הזין
-- );

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