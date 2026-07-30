document.addEventListener("DOMContentLoaded", function() {
    const liElements = document.querySelectorAll('nav ul li');
    const navArea = document.getElementById('navArea');

    let hoverIntent = false;

    // li/navArea 밖으로 마우스가 나갔을 때 바로 감추지 않고, 다음 mouseenter로 hoverIntent가
    // 다시 true가 될 시간을 잠깐 준 뒤에도 여전히 벗어나 있으면 감춘다(li -> navArea 이동 허용).
    function scheduleHide() {
        hoverIntent = false;
        setTimeout(() => {
            if (!hoverIntent) {
                navArea.classList.remove('show');
            }
        }, );
    }

    liElements.forEach(li => {
        li.addEventListener('mouseenter', () => {
            hoverIntent = true;
            navArea.classList.add('show');
            navArea.style.zIndex = 2;
        });

        li.addEventListener('mouseleave', scheduleHide);
    });

    navArea.addEventListener('mouseenter', () => {
        hoverIntent = true;
    });

    navArea.addEventListener('mouseleave', scheduleHide);
});

function myCheck(){
    alert("로그인 후 이용 가능한 기능입니다.");
    if(confirm("로그인 페이지로 이동하시겠습니까?")){
        location.href="/login";
    }
}

// 헤더 fragment(fragment/header.html)의 data-csrf-* 속성에서 세션 CSRF 토큰을 읽는다.
// 모든 페이지가 nav.js를 header.js보다 먼저 로드하므로, CSRF 헤더/토큰이 필요한
// 다른 스크립트(loginModal.js, findAccount.js 등)에서도 이 함수를 그대로 사용한다.
function getCsrfHeader(){
    const header = document.querySelector('header[data-csrf-token]');
    return header ? {name: header.dataset.csrfParam, value: header.dataset.csrfToken} : {name: '', value: ''};
}

function getCsrfToken(){
    return getCsrfHeader().value;
}

function outCheck(){
    if(confirm("로그아웃 하시겠습니까?")){
        const csrf = getCsrfHeader();
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/logout';

        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = csrf.name;
        csrfInput.value = csrf.value;
        form.appendChild(csrfInput);

        document.body.appendChild(form);
        form.submit();
    }
}