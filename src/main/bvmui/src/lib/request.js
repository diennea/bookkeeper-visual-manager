import axios from "axios";

export default {
    get(url, successCallback, errorCallback) {
        axios
            .get(url)
            .then(response => successCallback(response.data))
            .catch(errorCallback);
    },
    post(url, data = {}, successCallback, errorCallback) {
        axios
            .post(url, data)
            .then(response => successCallback(response.data))
            .catch(errorCallback);
    }
};
