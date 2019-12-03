<template>
    <div class="bvm-tile" @click="$emit('click', $event)">
        <span>Ledger {{item.id}}
        Size {{item.length}} bytes<br />
        Replication {{item.writeQuorumSize}}<br />
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


