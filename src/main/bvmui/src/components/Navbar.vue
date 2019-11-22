<template>
    <nav class="bvm-navbar">
        <ul class="bvm-navbar-content">
            <router-link class="bvm-navbar-item" active-class="selected"
            tag="li" to="/bookies" exact>
                <span class="bvm-nav-link">Bookies</span>
            </router-link>
            <router-link class="bvm-navbar-item" active-class="selected"
            tag="li" to="/ledgers" exact>
                <span class="bvm-nav-link">Ledgers</span>
            </router-link>
            <router-link class="bvm-navbar-item" active-class="selected"
            tag="li" to="/cache" exact>
                <span class="bvm-nav-link">Cache</span>
            </router-link>
        </ul>
    </nav>
</template>
<script>
export default {
    data: function () {
        return {

        }
    }
};
</script>

