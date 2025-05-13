const axios = require('axios');

async function testAddToCart() {
    try {
        const response = await axios.post('http://192.168.33.206:1234/cart/add', {
            userId: 1,
            foodId: 1
        }, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        console.log('Response:', response.data);
    } catch (error) {
        console.error('Error:', error.response ? error.response.data : error.message);
    }
}

testAddToCart(); 