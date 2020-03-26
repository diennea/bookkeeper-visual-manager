export default {
    formatBytes(bytes, decimals = 2) {
        if (bytes === 0) return '0 Bytes';

        const k = 1024;
        const dm = decimals < 0 ? 0 : decimals;
        const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

        const i = Math.floor(Math.log(bytes) / Math.log(k));

        return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
    },
    formatPercent(a, b, decimals = 2) {
        if (b === 0) return 'N/A';
        return parseFloat(a * 100 / b).toFixed(decimals);
    },
    replacePlaceholders(string, placeholders) {
        const _placeholders = !placeholders ? {} : placeholders;
        for (var placeholder in _placeholders) {
            string = string.replace(
                new RegExp("\\${" + placeholder + "}", "g"),
                placeholders[placeholder]
            );
        }
        return string;
    },
    formatDate(ts){
        if (ts){
            const months = ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"];
            let current_datetime = new Date()
            let formatted_date = current_datetime.getDate()  + " "
            + (months[current_datetime.getMonth()]) + " "
            + current_datetime.getFullYear() + " "
            + current_datetime.getHours() + ":"
            + (current_datetime.getMinutes() < 10 ? "0" + current_datetime.getMinutes() : current_datetime.getMinutes())
            return formatted_date;
        }
    }
}
