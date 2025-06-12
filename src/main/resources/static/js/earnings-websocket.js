// Connect to WebSocket
function connectToEarningsWebSocket(userId) {
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        console.log('Connected to earnings WebSocket');
        
        // Subscribe to personal earnings channel
        stompClient.subscribe(`/topic/earnings/${userId}`, function(message) {
            const earning = JSON.parse(message.body);
            
            // Create notification
            const notification = {
                title: 'New Earning!',
                body: `You earned ${earning.earnedAmount} from ${earning.referralName}`,
                icon: '/images/earning-icon.png'
            };
            
            // Show browser notification if permitted
            if (Notification.permission === "granted") {
                new Notification(notification.title, notification);
            }
            
            // Dispatch custom event for UI updates
            const event = new CustomEvent('newEarning', { detail: earning });
            document.dispatchEvent(event);
        });
    }, function(error) {
        console.error('Error connecting to WebSocket:', error);
        // Attempt to reconnect after 5 seconds
        setTimeout(() => connectToEarningsWebSocket(userId), 5000);
    });
    
    return stompClient;
}

// Example usage in your application:
/*
document.addEventListener('DOMContentLoaded', function() {
    // Request notification permission
    if (Notification.permission !== "granted") {
        Notification.requestPermission();
    }
    
    // Connect to WebSocket with current user's ID
    const userId = document.getElementById('user-id').value;
    const stompClient = connectToEarningsWebSocket(userId);
    
    // Listen for new earnings
    document.addEventListener('newEarning', function(e) {
        const earning = e.detail;
        // Update UI as needed
        updateEarningsDisplay(earning);
    });
});
*/ 