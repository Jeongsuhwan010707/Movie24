
////////네비게이터 수정중

document.addEventListener("DOMContentLoaded", function() {
    const liElements = document.querySelectorAll('nav ul li');
    const navArea = document.getElementById('navArea');

    let hoverIntent = false;

    liElements.forEach(li => {
        li.addEventListener('mouseenter', () => {
            hoverIntent = true;
            navArea.classList.add('show');
            navArea.style.zIndex = 2;
        });

        li.addEventListener('mouseleave', () => {
            hoverIntent = false;
            setTimeout(() => {
                if (!hoverIntent) {
                    navArea.classList.remove('show');
                }
                // navArea.style.zIndex = -1;
            }, ); // Add a delay before hiding to allow time for moving to menu_text
        });
    });

    navArea.addEventListener('mouseenter', () => {
        hoverIntent = true;
    });

    navArea.addEventListener('mouseleave', () => {
        hoverIntent = false;
        setTimeout(() => {
            if (!hoverIntent) {
                navArea.classList.remove('show');
            }
            // navArea.style.zIndex = -1;
        }, ); // Add a delay before hiding to allow time for moving to menu_text
    });
});


// -----------------------test

function myCheck(){
    const memberId = "${memberId}";
    if(memberId === ""){
        alert("로그인 후 이용가능한 기능입니다.");
        if(confirm("로그인 페이지로 이동하시겠습니까?")){
            location.href="/member/login.do";
        }
    }else{
        modal.style.display = "block";
    }
}
function outCheck(){
    if(confirm("로그아웃 하시겠습니까?")){
        location.href="/member/logout.do";
    }
}
function myCheck(){
    alert("로그인이 되어있지 않습니다.");
}