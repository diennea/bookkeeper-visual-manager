export default {
    createSession(token, role) {
        localStorage.setItem("user-token", token);
        localStorage.setItem("user-role", role)
    },
    destroySession() {
        localStorage.removeItem("user-token");
        localStorage.removeItem("user-role");
    },
    isLogged() {
        return localStorage.getItem("user-token") !== null;
    },
    LOGIN_ENDPOINT: 'api/auth/login',
    LOGOUT_ENDPOINT: 'api/auth/logout'
};
