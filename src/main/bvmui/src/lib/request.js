import axios from "axios";

export default {
  get(url, successCallback, errorCallback) {
    axios
      .get(url)
      .then(response => successCallback(response.data))
      .catch(errorCallback);
  }
};
