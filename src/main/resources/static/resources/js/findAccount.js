(function () {
    function csrfToken() {
        var header = document.querySelector('header[data-csrf-token]');
        return header ? header.dataset.csrfToken : '';
    }

    function clearResult(box) {
        box.innerHTML = '';
        box.classList.remove('find_error', 'find_result');
    }

    function showError(box, message) {
        clearResult(box);
        box.textContent = message;
        box.classList.add('find_error');
    }

    function postJson(url, body) {
        return fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json', 'X-CSRF-TOKEN': csrfToken()},
            body: JSON.stringify(body)
        }).then(function (res) {
            return res.json().catch(function () { return {}; }).then(function (data) {
                return {ok: res.ok, status: res.status, data: data};
            });
        });
    }

    function firstErrorMessage(data) {
        if (data && data.message) {
            return data.message;
        }
        if (data && typeof data === 'object') {
            var keys = Object.keys(data);
            if (keys.length > 0) {
                return data[keys[0]];
            }
        }
        return '요청 처리 중 오류가 발생했습니다.';
    }

    function initTabs() {
        var idBtn = document.getElementById('tabBtn-id');
        var pwBtn = document.getElementById('tabBtn-pw');
        var idPanel = document.getElementById('panel-id');
        var pwPanel = document.getElementById('panel-pw');

        function showTab(tab) {
            idPanel.classList.toggle('active', tab === 'id');
            pwPanel.classList.toggle('active', tab === 'pw');
            idBtn.classList.toggle('active', tab === 'id');
            pwBtn.classList.toggle('active', tab === 'pw');
        }

        idBtn.addEventListener('click', function () { showTab('id'); });
        pwBtn.addEventListener('click', function () { showTab('pw'); });
        showTab('id');
    }

    function initFindId() {
        var form = document.getElementById('findIdForm');
        var resultBox = document.getElementById('findIdResult');
        var submitBtn = form.querySelector('.find_submit');

        form.addEventListener('submit', function (e) {
            e.preventDefault();
            clearResult(resultBox);

            var name = document.getElementById('findIdName').value.trim();
            var email = document.getElementById('findIdEmail').value.trim();
            if (!name || !email) {
                showError(resultBox, '이름과 이메일을 입력해주세요.');
                return;
            }

            submitBtn.disabled = true;
            postJson('/api/users/find-id', {name: name, email: email})
                .then(function (result) {
                    submitBtn.disabled = false;
                    if (!result.ok) {
                        showError(resultBox, firstErrorMessage(result.data));
                        return;
                    }
                    renderFindIdResult(resultBox, result.data);
                })
                .catch(function () {
                    submitBtn.disabled = false;
                    showError(resultBox, '요청 처리 중 오류가 발생했습니다.');
                });
        });
    }

    function renderFindIdResult(box, data) {
        clearResult(box);
        box.classList.add('find_result');

        var maskedLoginIds = data.maskedLoginIds || [];
        var socialProviders = data.socialProviders || [];

        if (maskedLoginIds.length > 0) {
            var p1 = document.createElement('p');
            p1.appendChild(document.createTextNode('회원님의 아이디는 '));
            var strong = document.createElement('strong');
            strong.textContent = maskedLoginIds.join(', ');
            p1.appendChild(strong);
            p1.appendChild(document.createTextNode(' 입니다.'));
            box.appendChild(p1);
        }
        if (socialProviders.length > 0) {
            var p2 = document.createElement('p');
            var suffix = maskedLoginIds.length > 0
                ? '계정으로도 가입되어 있습니다. 해당 소셜 로그인도 함께 이용하실 수 있습니다.'
                : '계정으로 가입되어 있습니다. 해당 소셜 로그인을 이용해주세요.';
            p2.textContent = socialProviders.join(', ') + ' ' + suffix;
            box.appendChild(p2);
        }
    }

    function initFindPassword() {
        var form = document.getElementById('findPasswordForm');
        var resultBox = document.getElementById('findPasswordResult');
        var submitBtn = form.querySelector('.find_submit');

        form.addEventListener('submit', function (e) {
            e.preventDefault();
            clearResult(resultBox);

            var loginId = document.getElementById('findPwLoginId').value.trim();
            var name = document.getElementById('findPwName').value.trim();
            var email = document.getElementById('findPwEmail').value.trim();
            if (!loginId || !name || !email) {
                showError(resultBox, '아이디, 이름, 이메일을 모두 입력해주세요.');
                return;
            }

            submitBtn.disabled = true;
            postJson('/api/users/find-password/verify', {loginId: loginId, name: name, email: email})
                .then(function (result) {
                    submitBtn.disabled = false;
                    if (!result.ok) {
                        showError(resultBox, firstErrorMessage(result.data));
                        return;
                    }
                    window.location.href = '/users/reset-password';
                })
                .catch(function () {
                    submitBtn.disabled = false;
                    showError(resultBox, '요청 처리 중 오류가 발생했습니다.');
                });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        initTabs();
        initFindId();
        initFindPassword();
    });
})();
