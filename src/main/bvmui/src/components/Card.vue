<template>
    <div class="bvm-card" @click="$emit('click', $event)">
        <div class="bvm-card-status">
            <h1 v-if="data.ok">AVAILABLE</h1>
            <h1 v-else>NOT AVAILABLE</h1>
        </div>
        <div class="bvm-card-content">
            <ul>
                <li>
                    <span>Address: {{data.description}}</span>
                </li>
                <li v-if="data.ok">
                    <span>Free space: {{data.freeDiskSpace}}</span>
                </li>
                <li v-if="data.ok">
                    <span>Total space: {{data.totalDiskSpace}}</span>
                </li>
            </ul>
        </div>
    </div>
</template>
<script>
export default {
    props: {
        data: Object
    }
};
</script>


