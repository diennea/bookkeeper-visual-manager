export default {
    createSession(token) {
        localStorage.setItem("user-token", token);
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
