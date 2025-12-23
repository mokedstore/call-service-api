SELECT TOP (1000) [kId]
      ,[createdAt]
      ,[updatedAt]
      ,[siteNumber]
      ,[systemNumber]
      ,[alarmIncidentNumber]
      ,[dispatchLocation]
      ,[alaramEventId]
      ,[currentWriteEventCode]
      ,[fullClearStatus]
      ,[isActiveAlert]
      ,[alertHandlingStatusCode]
      ,[alertHandlingStatusMessage]
      ,[progressMessages]
      ,[contacts]
      ,[callGeneratedText]
      ,[textToSpeechFileLocation]
      ,[vonageCurrentConversationId]
      ,[answeredPhoneNumber]
      ,[orderOfAnsweredCall]
      ,[vonageConversationLength]
      ,[customerResponseToCall]
      ,[alertDate]
  FROM [Generic].[dbo].[Alerts]

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
--     N'ALERT-1234567891',
--     '2025-09-02T10:15:00Z',
--     '2025-09-02T11:00:00Z',
--     N'SITE-002',
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

ALTER TABLE Generic.dbo.Alerts
ADD csNumber NVARCHAR(40);

ALTER TABLE Generic.dbo.Alerts
DROP COLUMN alert_zone_id;

-- UPDATE  Generic.dbo.Alerts SET alertDate=GETDATE()

DELETE FROM Generic.dbo.Alerts;

SELECT * FROM Generic.dbo.Alerts;

SELECT * FROM Generic.dbo.TextToSpeechMessages;

SELECT kId FROM Generic.dbo.Alerts WHERE vonageCurrentConversationId = '';

USE Generic
EXEC sp_rename 'DispathIdToPhoneNumber', 'DispatchIdToPhoneNumber';

SELECT dispatchPhoneNumber FROM Generic.dbo.DispatchIdToPhoneNumber WHERE  dispatchLocation='101';

SELECT DISTINCT dispatchLocation FROM Generic.dbo.Alerts WHERE vonageCurrentConversationId = '501820a3-edad-4432-b857-f87e0aeb3329';

UPDATE Generic.dbo.DispatchIdToPhoneNumber SET dispatchPhoneNumber='972544888673' WHERE dispatchLocation='101'

UPDATE Generic.dbo.Alerts SET isActiveAlert=1 WHERE alarmIncidentNumber='1402229120'


INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('CIE755', 'CAMSIG');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('755', 'CAMSIG');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('CIE360', '9.הקלט')
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('CIE609', '9.הקלט');

INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('CIE350', '2.חשמל');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('350', '2.חשמל');

INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('CIE301', '2.חשמל');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('301', 'תק-תק');

INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('902', '5.דריכה');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('902S', '5.דריכה');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('9020S', '5.דריכה');

INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('399', '1.פתיח');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('3999', '1.פתיח');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('399S', '1.פתיח');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('3999S', '1.פתיח');

INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('CIE400', '1.פתיח');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('CIE401', '1.פתיח');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('CIE403', '1.פתיח');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('CIE404', '1.פתיח');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('CIE405', '1.פתיח');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('CIE406', '1.פתיח');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('CIE407', '1.פתיח');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('CIE408', '1.פתיח');
INSERT INTO Generic.dbo.TextToSpeechMessages VALUES('CIE409', '1.פתיח');