import axios from "axios";

export default {
    async get(url) {
        const response = await axios.get(url);
        return response.data;
    },
    async post(url, data = {}) {
        const response = await axios.post(url, data);
        return response.data;
    },
    async put(url, data = {}) {
        const response = await axios.put(url, data);
        return response.data;
    }
};
