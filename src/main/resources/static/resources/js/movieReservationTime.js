// 상영시간표 페이지: 영화/상영중-상영종료/날짜/지역 클릭 시 새로고침 없이
// #scheduleContent 영역만 서버에서 fragment로 받아와 교체한다.
// (좌석 선택으로 넘어가는 /movieReservation/seat 링크는 그대로 일반 이동으로 둔다.)
document.addEventListener('DOMContentLoaded', function () {
    const content = document.getElementById('scheduleContent');
    if (!content) {
        return;
    }

    function isFilterLink(a) {
        return a && a.href && a.href.indexOf('/movieReservation/time') !== -1;
    }

    function loadContent(url, pushState) {
        fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
            .then(function (res) {
                if (!res.ok) {
                    throw new Error('failed to load schedule: ' + res.status);
                }
                return res.text();
            })
            .then(function (html) {
                content.innerHTML = html;
                if (pushState) {
                    history.pushState({ movie24ScheduleUrl: url }, '', url);
                }
            })
            .catch(function () {
                // fetch가 실패하면(네트워크 오류 등) 일반 페이지 이동으로 대체한다.
                window.location.href = url;
            });
    }

    content.addEventListener('click', function (e) {
        const a = e.target.closest('a');
        if (!isFilterLink(a)) {
            return;
        }
        e.preventDefault();
        loadContent(a.href, true);
    });

    window.addEventListener('popstate', function () {
        loadContent(location.href, false);
    });

    if (!history.state || !history.state.movie24ScheduleUrl) {
        history.replaceState({ movie24ScheduleUrl: location.href }, '', location.href);
    }
});
