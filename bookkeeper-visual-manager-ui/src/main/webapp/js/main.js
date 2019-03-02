function getServerStatus() {
    fetch('http://localhost:8080/api/status')
        .then((res) => res.json())
        .then((json) => setResult('result', json))
        .catch(() => setResult('result', null));
}

function setResult(elementId, result) {
    const el = document.getElementById(elementId);
    el.classList.remove('hidden');

    if (!result) {
        el.classList.add('error');
        el.innerHTML = "down".toUpperCase();
    } else {
        el.classList.add('ok');
        el.innerHTML = result.status.toUpperCase();
    }

}
