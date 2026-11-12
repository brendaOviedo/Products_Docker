document.querySelectorAll('form[data-confirmar]').forEach(function (formulario) {
    formulario.addEventListener('submit', function (evento) {
        if (!window.confirm(formulario.dataset.confirmar)) {
            evento.preventDefault();
        }
    });
});

document.querySelectorAll('.alerta--exito').forEach(function (alerta) {
    setTimeout(function () {
        alerta.classList.add('alerta--oculta');
    }, 5000);
});
