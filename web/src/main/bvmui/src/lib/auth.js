import axios from "axios";

export default {
    getSession() {
        return localStorage.getItem("user-token") || '';
    },
    createSession(token) {
        localStorage.setItem("user-token", token);
        axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    },
    destroySession() {
        localStorage.removeItem("user-token");
        delete axios.defaults.headers.common["Authorization"];
    },
    isLogged() {
        return localStorage.getItem("user-token") !== null;
    },
    LOGIN_ENDPOINT: 'api/auth/login',
    LOGOUT_ENDPOINT: 'api/auth/logout'
};
