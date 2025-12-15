const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

/**
 * Cloud Function triggered when a new notification is added to Firestore
 * Automatically sends FCM notifications to all registered users
 */
exports.sendNotificationToAllUsers = functions.firestore
    .document('notification_data/{docId}')
    .onCreate(async (snap, context) => {
        
        const notificationData = snap.data();
        
        // Check if we should send notification
        if (!notificationData.shouldSendNotification) {
            console.log('shouldSendNotification is false, skipping...');
            return null;
        }
        
        try {
            // Get all FCM tokens from the fcm_tokens collection
            const tokensSnapshot = await admin.firestore()
                .collection('fcm_tokens')
                .get();
            
            if (tokensSnapshot.empty) {
                console.log('No FCM tokens found');
                return null;
            }
            
            // Extract all tokens
            const tokens = [];
            tokensSnapshot.forEach(doc => {
                const data = doc.data();
                if (data.fcmToken) {
                    tokens.push(data.fcmToken);
                }
            });
            
            if (tokens.length === 0) {
                console.log('No valid tokens to send to');
                return null;
            }
            
            console.log(`Sending notification to ${tokens.length} users`);
            
            // Prepare the notification message
            const message = {
                notification: {
                    title: notificationData.schemeName || 'New Scheme Available',
                    body: notificationData.notificationText || 'Check out the latest update!',
                },
                data: {
                    schemeName: notificationData.schemeName || '',
                    bookingDate: notificationData.bookingDate || '',
                    location: notificationData.location || '',
                    // Add image URLs if needed
                    image1: notificationData.image1 || '',
                    image2: notificationData.image2 || '',
                    click_action: 'FLUTTER_NOTIFICATION_CLICK'
                }
            };
            
            // Send to all tokens in batches (FCM allows max 500 tokens per request)
            const batchSize = 500;
            const batches = [];
            
            for (let i = 0; i < tokens.length; i += batchSize) {
                const batch = tokens.slice(i, i + batchSize);
                batches.push(
                    admin.messaging().sendEachForMulticast({
                        tokens: batch,
                        notification: message.notification,
                        data: message.data,
                        android: {
                            priority: 'high',
                            notification: {
                                sound: 'default',
                                channelId: 'default'
                            }
                        },
                        apns: {
                            payload: {
                                aps: {
                                    sound: 'default'
                                }
                            }
                        }
                    })
                );
            }
            
            // Wait for all batches to complete
            const responses = await Promise.all(batches);
            
            // Count successes and failures
            let successCount = 0;
            let failureCount = 0;
            const invalidTokens = [];
            
            responses.forEach((response, batchIndex) => {
                successCount += response.successCount;
                failureCount += response.failureCount;
                
                // Collect invalid tokens to remove them
                response.responses.forEach((resp, index) => {
                    if (!resp.success) {
                        const tokenIndex = batchIndex * batchSize + index;
                        if (resp.error?.code === 'messaging/invalid-registration-token' ||
                            resp.error?.code === 'messaging/registration-token-not-registered') {
                            invalidTokens.push(tokens[tokenIndex]);
                        }
                        console.error(`Error sending to token ${tokenIndex}:`, resp.error);
                    }
                });
            });
            
            console.log(`Notifications sent successfully: ${successCount}`);
            console.log(`Failed: ${failureCount}`);
            
            // Clean up invalid tokens
            if (invalidTokens.length > 0) {
                console.log(`Cleaning up ${invalidTokens.length} invalid tokens`);
                const deletePromises = invalidTokens.map(token => 
                    admin.firestore()
                        .collection('fcm_tokens')
                        .where('fcmToken', '==', token)
                        .get()
                        .then(snapshot => {
                            snapshot.forEach(doc => doc.ref.delete());
                        })
                );
                await Promise.all(deletePromises);
            }
            
            // Update the document to mark notification as sent
            await snap.ref.update({
                notificationSent: true,
                notificationSentAt: admin.firestore.FieldValue.serverTimestamp(),
                recipientCount: successCount
            });
            
            return {
                success: true,
                sent: successCount,
                failed: failureCount
            };
            
        } catch (error) {
            console.error('Error sending notifications:', error);
            
            // Mark as failed
            await snap.ref.update({
                notificationSent: false,
                notificationError: error.message
            });
            
            throw error;
        }
    });