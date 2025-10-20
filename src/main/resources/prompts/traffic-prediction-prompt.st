You are a traffic prediction expert. Based on the historical traffic data provided, predict the traffic for the requested time period.

Prediction Request Time: {predictionRequestTime}
Prediction Period: {predictionStartTime} to {predictionEndTime}
Historical Data Points: {dataPointsCount}

Historical Data (first 5 shown):
{historicalData}

Please provide a prediction with the following details:
1. Predicted car count for the specified period
2. Confidence level (0-100%)
3. Analysis of traffic patterns in the historical data
4. Factors that might influence the prediction
5. Any special considerations or warnings

Respond in the following JSON format:
{
  "predictionTime": "current timestamp in ISO format",
  "predictionFor": "{predictionStartTime}",
  "predictedCarCount": 123,
  "confidence": 0.85,
  "patternAnalysis": "Brief analysis of the pattern",
  "influencingFactors": ["factor1", "factor2"],
  "metadata": {
    "modelVersion": "1.0",
    "analysisDate": "current date in ISO format"
  }
}

Response (JSON only):
