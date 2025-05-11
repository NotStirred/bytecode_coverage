function darkMode() {
    document.documentElement.setAttribute('data-theme', 'dark');

    try {
        window.sessionStorage.setItem("light-dark-mode", "dark");
    } catch (e) {
    }

    let sw = document.getElementById("light-dark-mode-switch");
    sw.textContent = "Light Mode";
    sw.setAttribute("onClick", "lightMode()");
}

function lightMode() {
    document.documentElement.setAttribute('data-theme', 'light');

    try {
        window.sessionStorage.setItem("light-dark-mode", "light");
    } catch (e) {
    }

    let sw = document.getElementById("light-dark-mode-switch");
    sw.textContent = "Dark Mode";
    sw.setAttribute("onClick", "darkMode()");
}

try {
    let mode = window.sessionStorage.getItem("light-dark-mode");
    if (mode === "dark") {
        darkMode();
    } else if (mode === "light") {
        lightMode();
    }
} catch (e) {
}
