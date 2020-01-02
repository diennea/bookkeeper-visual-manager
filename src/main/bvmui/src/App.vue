<template>
    <div id="app">
        <v-app id="inspire">
            <Drawer v-if="$store.state.logged" />
            <Navbar v-if="$store.state.logged" />
            <Page />
        </v-app>
    </div>
</template>
