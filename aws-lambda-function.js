// AWS Lambda Function for Bus Location Updates
// This is a sample implementation that you can deploy to AWS Lambda

const AWS = require('aws-sdk');
const dynamodb = new AWS.DynamoDB.DocumentClient();

exports.handler = async (event) => {
    const headers = {
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Headers': 'Content-Type,Authorization',
        'Access-Control-Allow-Methods': 'OPTIONS,POST,GET'
    };
    
    try {
        const method = event.httpMethod;
        const path = event.path;
        
        if (method === 'OPTIONS') {
            return {
                statusCode: 200,
                headers,
                body: JSON.stringify({ message: 'CORS preflight' })
            };
        }
        
        if (method === 'POST' && path === '/driver/login') {
            return await handleDriverLogin(event, headers);
        }
        
        if (method === 'POST' && path === '/bus/location') {
            return await handleLocationUpdate(event, headers);
        }
        
        return {
            statusCode: 404,
            headers,
            body: JSON.stringify({ 
                success: false, 
                message: 'Endpoint not found' 
            })
        };
        
    } catch (error) {
        console.error('Error:', error);
        return {
            statusCode: 500,
            headers,
            body: JSON.stringify({ 
                success: false, 
                message: 'Internal server error',
                error: error.message 
            })
        };
    }
};

async function handleDriverLogin(event, headers) {
    const body = JSON.parse(event.body);
    const { busId, driverName } = body;
    
    if (!busId) {
        return {
            statusCode: 400,
            headers,
            body: JSON.stringify({ 
                success: false, 
                message: 'Bus ID is required' 
            })
        };
    }
    
    // Validate bus exists in system
    const busParams = {
        TableName: 'Buses',
        Key: { busId }
    };
    
    try {
        const busResult = await dynamodb.get(busParams).promise();
        
        if (!busResult.Item) {
            return {
                statusCode: 404,
                headers,
                body: JSON.stringify({ 
                    success: false, 
                    message: 'Bus ID not found' 
                })
            };
        }
        
        // Create session record
        const sessionParams = {
            TableName: 'DriverSessions',
            Item: {
                busId,
                driverName: driverName || 'Unknown',
                loginTime: new Date().toISOString(),
                isActive: true,
                sessionId: `${busId}-${Date.now()}`
            }
        };
        
        await dynamodb.put(sessionParams).promise();
        
        return {
            statusCode: 200,
            headers,
            body: JSON.stringify({
                success: true,
                message: 'Login successful',
                token: `session-${busId}-${Date.now()}`,
                busInfo: {
                    busId,
                    routeName: busResult.Item.routeName,
                    routeNumber: busResult.Item.routeNumber,
                    isActive: true
                }
            })
        };
        
    } catch (error) {
        console.error('Login error:', error);
        return {
            statusCode: 500,
            headers,
            body: JSON.stringify({ 
                success: false, 
                message: 'Login failed' 
            })
        };
    }
}

async function handleLocationUpdate(event, headers) {
    const body = JSON.parse(event.body);
    const { busId, latitude, longitude, speed, timestamp, bearing, accuracy, status } = body;
    
    if (!busId || !latitude || !longitude) {
        return {
            statusCode: 400,
            headers,
            body: JSON.stringify({ 
                success: false, 
                message: 'Missing required location data' 
            })
        };
    }
    
    // Store location update
    const locationParams = {
        TableName: 'BusLocations',
        Item: {
            busId,
            timestamp: timestamp || Date.now(),
            latitude: Number(latitude),
            longitude: Number(longitude),
            speed: Number(speed) || 0,
            bearing: Number(bearing) || 0,
            accuracy: Number(accuracy) || 0,
            status: status || 'unknown',
            updatedAt: new Date().toISOString()
        }
    };
    
    try {
        await dynamodb.put(locationParams).promise();
        
        // Update bus current status
        const busUpdateParams = {
            TableName: 'Buses',
            Key: { busId },
            UpdateExpression: 'SET lastLatitude = :lat, lastLongitude = :lng, lastSpeed = :speed, lastUpdate = :time, #status = :status',
            ExpressionAttributeNames: {
                '#status': 'status'
            },
            ExpressionAttributeValues: {
                ':lat': Number(latitude),
                ':lng': Number(longitude),
                ':speed': Number(speed) || 0,
                ':time': new Date().toISOString(),
                ':status': status || 'unknown'
            }
        };
        
        await dynamodb.update(busUpdateParams).promise();
        
        return {
            statusCode: 200,
            headers,
            body: JSON.stringify({
                success: true,
                message: 'Location updated successfully'
            })
        };
        
    } catch (error) {
        console.error('Location update error:', error);
        return {
            statusCode: 500,
            headers,
            body: JSON.stringify({ 
                success: false, 
                message: 'Failed to update location' 
            })
        };
    }
}

// DynamoDB Table Schema:
/*

Buses Table:
{
    "busId": "BUS001",
    "routeName": "Downtown Express",
    "routeNumber": "R1",
    "capacity": 50,
    "isActive": true,
    "lastLatitude": 0.0,
    "lastLongitude": 0.0,
    "lastSpeed": 0.0,
    "status": "idle",
    "lastUpdate": "2025-01-01T00:00:00Z"
}

BusLocations Table:
{
    "busId": "BUS001",
    "timestamp": 1640995200000,
    "latitude": 37.7749,
    "longitude": -122.4194,
    "speed": 25.5,
    "bearing": 180.0,
    "accuracy": 5.0,
    "status": "moving",
    "updatedAt": "2025-01-01T00:00:00Z"
}

DriverSessions Table:
{
    "busId": "BUS001",
    "sessionId": "BUS001-1640995200000",
    "driverName": "John Doe",
    "loginTime": "2025-01-01T00:00:00Z",
    "isActive": true
}

*/
