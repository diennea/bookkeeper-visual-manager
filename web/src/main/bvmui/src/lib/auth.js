import axios from "axios";

export default {
    getSession() {
        return localStorage.getItem("user-token") || '';
    },
    createSession() {
        localStorage.setItem("user-token", 'dummy');
    },
    destroySession() {
        localStorage.removeItem("user-token");
    },
    isLogged() {
        return localStorage.getItem("user-token") !== null;
    },
    LOGIN_ENDPOINT: 'api/auth/login',
    LOGOUT_ENDPOINT: 'api/auth/logout'
};
