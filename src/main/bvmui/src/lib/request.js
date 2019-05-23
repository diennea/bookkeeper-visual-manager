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
                { description: "127.0.0.1:8080", ok: true, freeDiskSpace: 12433813504, totalDiskSpace: 12433813504 },
                { description: "127.0.0.1:8081", ok: true, freeDiskSpace: 10436613504, totalDiskSpace: 12433813504 },
                { description: "127.0.0.1:8082", ok: true, freeDiskSpace: 933813504, totalDiskSpace: 12433813504 },
            ]
        } else {
            return []
        }
    }
    if (url.includes("api/ledger")) {
        var ledger = url.replace("api/ledger/", "");
        if (ledger === "all") {
            return [
                { id: 1, metadata: {} },
                { id: 2, metadata: {} },
                { id: 3, metadata: {} }
            ];
        } else if (url.includes("bookie")) {
            var bookie = ledger.replace("bookie/", "");
            return [
                { id: 1, metadata: {} }
            ]
        } else if (url.includes("metadata")) {
            return {
                application: "bookkeper",
                data: "data-bookkeeper",
            }
        }
    }
    return null;
}
