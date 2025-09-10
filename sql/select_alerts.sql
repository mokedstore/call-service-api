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

-- ALTER TABLE Generic.dbo.Alerts
-- ADD alertDate DATETIME2 DEFAULT SYSDATETIME();

-- UPDATE  Generic.dbo.Alerts SET alertDate=GETDATE()

DELETE FROM Generic.dbo.Alerts;

SELECT * FROM Generic.dbo.Alerts;

SELECT * FROM Generic.dbo.TextToSpeechMessages;