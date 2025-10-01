SELECT TOP (1000) [conversationId]
      ,[uuid]
      ,[fromNo]
      ,[toNo]
      ,[eventTimestamp]
      ,[disconnectedBy]
      ,[duration]
      ,[rate]
      ,[price]
      ,[startTime]
      ,[endTime]
      ,[rawEvent]
      ,[kId]
      ,[status]
  FROM [Generic].[dbo].[Conversations]

ALTER TABLE Generic.dbo.Conversations
ADD STATUS NVARCHAR(50);

SELECT * FROM Generic.dbo.Conversations;

SELECT conversationId, uuid, status FROM Generic.dbo.Conversations ORDER BY eventTimestamp ASC;


SELECT COUNT(*) AS count FROM Generic.dbo.Conversations WHERE (uuid = 'CON-e3187b08-55c7-4750-8ba2-584beb0fefa7' OR conversationId = 'CON-e3187b08-55c7-4750-8ba2-584beb0fefa7')
AND status  LIKE 'dtmf%';


DELETE FROM [Generic].[dbo].[Conversations];