<template>
    <div class="bvm-tile" @click="$emit('click', $event)">
        <span>Ledger {{item.id}}
        Size {{$library.formatBytes(item.length)}}<br />
        Replication {{item.writeQuorumSize}}<br />
        Age {{item.age}} hours<br />
        </span>
    </div>
</template>
<script>
export default {
    props: {
        item: Object
    }
};
</script>


