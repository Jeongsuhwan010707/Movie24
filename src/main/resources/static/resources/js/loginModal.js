// 로그인 여부가 필요한 동작(상영시간표 -> 좌석선택, 좌석선택 -> 결제, 결제 요청) 앞에
// requireLogin(action)으로 감싸면: 로그인 상태면 즉시 action()을 실행하고,
// 아니면 공용 로그인 모달(fragment/loginModal.html)을 띄워 로그인 성공 후에 action()을 이어서 실행한다.
(function () {
    var overlay, form, idInput, pwInput, errorBox, submitBtn, closeBtn;
    var pendingAction = null;

    function bindOnce() {
        if (overlay) {
            return true;
        }
        overlay = document.getElementById('loginModalOverlay');
        if (!overlay) {
            return false;
        }
        form = document.getElementById('loginModalForm');
        idInput = document.getElementById('loginModalId');
        pwInput = document.getElementById('loginModalPassword');
        errorBox = document.getElementById('loginModalError');
        submitBtn = document.getElementById('loginModalSubmit');
        closeBtn = document.getElementById('loginModalClose');

        form.addEventListener('submit', submitLogin);
        closeBtn.addEventListener('click', closeModal);
        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) {
                closeModal();
            }
        });
        return true;
    }

    function showError(msg) {
        errorBox.textContent = msg;
        errorBox.classList.add('show');
    }

    function hideError() {
        errorBox.classList.remove('show');
    }

    function openModal(action) {
        if (!bindOnce()) {
            return;
        }
        pendingAction = action;
        hideError();
        form.reset();
        overlay.classList.add('open');
        idInput.focus();
    }

    function closeModal() {
        if (overlay) {
            overlay.classList.remove('open');
        }
        pendingAction = null;
    }

    function submitLogin(e) {
        e.preventDefault();
        hideError();
        var loginId = idInput.value.trim();
        var password = pwInput.value;
        if (!loginId || !password) {
            showError('아이디와 비밀번호를 입력해주세요.');
            return;
        }

        submitBtn.disabled = true;
        fetch('/api/login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrfToken()},
            body: JSON.stringify({loginId: loginId, password: password})
        }).then(function (res) {
            submitBtn.disabled = false;
            if (res.status === 401) {
                showError('아이디 또는 비밀번호가 올바르지 않습니다.');
                return;
            }
            if (!res.ok) {
                showError('로그인 중 오류가 발생했습니다.');
                return;
            }
            var action = pendingAction;
            closeModal();
            if (action) {
                action();
            }
        }).catch(function () {
            submitBtn.disabled = false;
            showError('로그인 중 오류가 발생했습니다.');
        });
    }

    function csrfToken() {
        var header = document.querySelector('header[data-csrf-token]');
        return header ? header.dataset.csrfToken : '';
    }

    function requireLogin(action) {
        fetch('/api/users/me', {headers: {'X-Requested-With': 'XMLHttpRequest'}})
            .then(function (res) {
                if (res.ok) {
                    action();
                } else {
                    openModal(action);
                }
            })
            .catch(function () {
                openModal(action);
            });
    }

    window.requireLogin = requireLogin;
})();
