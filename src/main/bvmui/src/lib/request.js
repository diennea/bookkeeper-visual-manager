import axios from 'axios';

export default {
    get(url, successCallback, errorCallback) {
        if (process.env.NODE_ENV === "production") {
            axios.get(url)
                .then(response => successCallback(response.data))
                .catch(errorCallback);
        } else {
            var result = mockRequest(url);
            if (!result) {
                errorCallback();
            } else {
                successCallback(result);
            }
        }
    }
}

function mockRequest(url) {
    if (url.includes("api/bookie")) {
        var bookie = url.replace("api/bookie/", "");
        if (bookie === "all") {
            return [
                { description: "127.0.0.1:8080", ok: true, freeDiskSpace: 1000, totalDiskSpace: 12433813504 },
                { description: "127.0.0.1:8081", ok: true, freeDiskSpace: 1000, totalDiskSpace: 12433813504 },
                { description: "127.0.0.1:8082", ok: true, freeDiskSpace: 1000, totalDiskSpace: 12433813504 },
            ]
        } else {
            return []
        }
    }
    if (url.includes("api/ledger")) {
        var ledger = url.replace("api/ledger/", "");
        if (ledger === "all") {
            return [];
        } else if (url.includes("bookie")) {
            var bookie = ledger.replace("bookie/", "");
            return []
        }
    }
    return null;
}
