<template>
    <div class="bvm-card" @click="$emit('click', $event)">
        <div class="bvm-card-description">{{data.description}}</div>
        <div class="bvm-card-status">
            <h1 >{{data.state}}</h1>
        </div>
        <div class="bvm-card-content">
            <ul>
                <li v-if="data.totalDiskSpace > 0">
                    <span>Free space: {{$library.formatBytes(data.freeDiskSpace)}} - {{$library.formatPercent(data.freeDiskSpace, data.totalDiskSpace)}} %</span>
                </li>
                <li v-if="data.totalDiskSpace > 0">
                    <span>Total space: {{$library.formatBytes(data.totalDiskSpace)}}</span>
                </li>
                <li>
                    <span>Sampled at {{new Date(data.lastScan)}}.</span>
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


