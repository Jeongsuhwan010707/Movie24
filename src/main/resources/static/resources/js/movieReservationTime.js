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

    // 날짜 목록(#dateTrack)이 뷰포트(#dateViewport)보다 넓은 경우, 화살표를 눌러
    // 한 칸씩 슬라이드시킨다. 맨 앞(오늘)에서는 왼쪽 화살표가 더 이상 움직이지 않는다.
    // 오른쪽은 마지막 날짜가 뷰포트 오른쪽 끝에 딱 붙지 않도록, 끝까지 다 보여주는
    // 지점보다 한 칸 덜 이동한 곳에서 멈춘다(심미적 여백용 — 날짜 개수 자체는 그대로).
    function initDateScroller() {
        const viewport = content.querySelector('#dateViewport');
        const track = content.querySelector('#dateTrack');
        const backBtn = content.querySelector('#dateBack');
        const nextBtn = content.querySelector('#dateNext');
        if (!viewport || !track || !backBtn || !nextBtn) {
            return;
        }

        const items = Array.prototype.slice.call(track.children);
        if (items.length === 0) {
            return;
        }

        const itemWidth = items[0].getBoundingClientRect().width;
        const visibleCount = Math.max(1, Math.floor(viewport.clientWidth / itemWidth));
        const maxIndex = Math.max(0, items.length - visibleCount - 1);

        const selectedLi = track.querySelector('li.selected');
        const selectedIndex = selectedLi ? items.indexOf(selectedLi) : 0;
        let index = Math.min(Math.max(selectedIndex, 0), maxIndex);

        function render(animate) {
            track.style.transition = animate ? 'transform .3s ease' : 'none';
            track.style.transform = 'translateX(-' + (index * itemWidth) + 'px)';
            backBtn.classList.toggle('disabled', index === 0);
            nextBtn.classList.toggle('disabled', index === maxIndex);
        }

        backBtn.onclick = function () {
            if (index === 0) {
                return;
            }
            index -= 1;
            render(true);
        };
        nextBtn.onclick = function () {
            if (index === maxIndex) {
                return;
            }
            index += 1;
            render(true);
        };

        render(false);
    }

    // 달력 아이콘을 누르면 모달을 띄운다. 날짜 스크롤러(#dateTrack)에 이미 서버가
    // "오늘 ~ 최대 날짜" 범위로 렌더링해 둔 <a data-date> 들을 그대로 선택 가능한
    // 날짜 목록으로 재사용한다 — 그 범위 밖의 날은 달력에서 그냥 비활성으로 둔다.
    // 날짜를 고르면(레이아웃이 바뀌어도 되므로) 전체 content를 다시 불러와 이동한다.
    function initCalendarModal() {
        const calBtn = content.querySelector('#dateCalendarBtn');
        const overlay = content.querySelector('#calendarModalOverlay');
        const track = content.querySelector('#dateTrack');
        if (!calBtn || !overlay || !track) {
            return;
        }
        const prevBtn = overlay.querySelector('#calPrevMonth');
        const nextBtn = overlay.querySelector('#calNextMonth');
        const monthLabel = overlay.querySelector('#calMonthLabel');
        const grid = overlay.querySelector('#calGrid');
        const closeBtn = overlay.querySelector('#calCloseBtn');

        const validDates = {};
        Array.prototype.forEach.call(track.querySelectorAll('a[data-date]'), function (a) {
            validDates[a.getAttribute('data-date')] = a.href;
        });
        const dateKeys = Object.keys(validDates).sort();
        if (dateKeys.length === 0) {
            return;
        }
        const todayStr = dateKeys[0];
        const minStr = dateKeys[0];
        const maxStr = dateKeys[dateKeys.length - 1];

        function currentSelectedStr() {
            const selectedLi = track.querySelector('li.selected a[data-date]');
            return selectedLi ? selectedLi.getAttribute('data-date') : todayStr;
        }

        let viewYear;
        let viewMonth;

        function monthKey(y, m) {
            return y * 12 + (m - 1);
        }
        const minMonthKey = monthKey(Number(minStr.slice(0, 4)), Number(minStr.slice(5, 7)));
        const maxMonthKey = monthKey(Number(maxStr.slice(0, 4)), Number(maxStr.slice(5, 7)));

        function render() {
            monthLabel.textContent = viewYear + '년 ' + viewMonth + '월';
            prevBtn.disabled = monthKey(viewYear, viewMonth) <= minMonthKey;
            nextBtn.disabled = monthKey(viewYear, viewMonth) >= maxMonthKey;

            grid.innerHTML = '';
            const firstWeekday = new Date(viewYear, viewMonth - 1, 1).getDay();
            const daysInMonth = new Date(viewYear, viewMonth, 0).getDate();

            for (let i = 0; i < firstWeekday; i++) {
                grid.appendChild(document.createElement('span'));
            }
            for (let day = 1; day <= daysInMonth; day++) {
                const dateStr = viewYear + '-' + String(viewMonth).padStart(2, '0') + '-' + String(day).padStart(2, '0');
                const btn = document.createElement('button');
                btn.type = 'button';
                btn.textContent = String(day);
                if (dateStr === todayStr) {
                    btn.classList.add('today');
                }
                if (Object.prototype.hasOwnProperty.call(validDates, dateStr)) {
                    btn.addEventListener('click', function () {
                        overlay.style.display = 'none';
                        loadContent(validDates[dateStr], true);
                    });
                } else {
                    btn.disabled = true;
                }
                grid.appendChild(btn);
            }
        }

        calBtn.onclick = function () {
            const startStr = currentSelectedStr();
            viewYear = Number(startStr.slice(0, 4));
            viewMonth = Number(startStr.slice(5, 7));
            render();
            overlay.style.display = 'flex';
        };
        closeBtn.onclick = function () {
            overlay.style.display = 'none';
        };
        overlay.onclick = function (e) {
            if (e.target === overlay) {
                overlay.style.display = 'none';
            }
        };
        prevBtn.onclick = function () {
            if (prevBtn.disabled) {
                return;
            }
            viewMonth -= 1;
            if (viewMonth < 1) {
                viewMonth = 12;
                viewYear -= 1;
            }
            render();
        };
        nextBtn.onclick = function () {
            if (nextBtn.disabled) {
                return;
            }
            viewMonth += 1;
            if (viewMonth > 12) {
                viewMonth = 1;
                viewYear += 1;
            }
            render();
        };
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
                initDateScroller();
                initCalendarModal();
                if (pushState) {
                    history.pushState({ movie24ScheduleUrl: url }, '', url);
                }
            })
            .catch(function () {
                // fetch가 실패하면(네트워크 오류 등) 일반 페이지 이동으로 대체한다.
                window.location.href = url;
            });
    }

    // 영화/극장/상영중-상영종료/지역/날짜 링크들은 서로의 상태(movieId, theaterId, date 등)를
    // 쿼리 파라미터로 그대로 실어 나르는데, 가벼운 갱신(날짜만/영화·극장만 변경)은 위쪽
    // 선택창 전체를 다시 그리지 않으므로 남아있는 다른 필터 링크들의 파라미터 값을
    // 새 값으로 맞춰줘야 한다(안 그러면 다음 필터 클릭에서 방금 고른 값이 사라진다).
    // excludeSelectors에 해당하는 조상(자기 자신을 가리켜야 하는 링크 그룹)은 건드리지 않는다.
    function syncParam(paramName, value, excludeSelectors) {
        content.querySelectorAll('a[href*="/movieReservation/time"]').forEach(function (a) {
            if (excludeSelectors.some(function (sel) { return a.closest(sel); })) {
                return;
            }
            const url = new URL(a.href, location.href);
            if (!url.searchParams.has(paramName)) {
                return;
            }
            url.searchParams.set(paramName, value);
            a.href = url.toString();
        });
    }

    // 위쪽 선택창(영화/극장 선택, 날짜 스크롤러 등)은 그대로 두고 아래 일정 목록
    // (#scheduleList)만 서버에서 새로 받아 교체한다. 날짜만/영화·극장만 바뀐 경우
    // 공통으로 쓰는 마무리 단계.
    function swapScheduleList(url) {
        const scheduleList = content.querySelector('#scheduleList');
        if (!scheduleList) {
            window.location.href = url;
            return;
        }
        fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest', 'X-Partial': 'schedule-list' } })
            .then(function (res) {
                if (!res.ok) {
                    throw new Error('failed to load schedule: ' + res.status);
                }
                return res.text();
            })
            .then(function (html) {
                scheduleList.innerHTML = html;
                history.pushState({ movie24ScheduleUrl: url }, '', url);
            })
            .catch(function () {
                // fetch가 실패하면(네트워크 오류 등) 일반 페이지 이동으로 대체한다.
                window.location.href = url;
            });
    }

    // 날짜만 선택했을 때는 화면이 흔들리지 않도록, 위쪽 영화/극장 선택창과 날짜
    // 스크롤러는 그대로 두고 밑줄 표시만 즉시 바꾼 뒤 일정 목록만 갱신한다.
    function loadDateOnly(a) {
        const url = new URL(a.href, location.href);
        const newDateStr = url.searchParams.get('date');
        const track = content.querySelector('#dateTrack');
        if (!newDateStr || !track) {
            loadContent(a.href, true);
            return;
        }

        const clickedLi = a.closest('li');
        const previousLi = track.querySelector('li.selected');
        if (previousLi) {
            previousLi.classList.remove('selected');
        }
        if (clickedLi) {
            clickedLi.classList.add('selected');
        }
        syncParam('date', newDateStr, ['#dateTrack']);
        swapScheduleList(a.href);
    }

    // 영화별/극장별 목록에서 다른 항목을 고른 경우에도 날짜 스크롤러 위치를 포함해
    // 위쪽 선택창은 그대로 두고, 고른 항목의 밑줄 표시·배너 텍스트만 즉시 바꾼 뒤
    // 일정 목록만 갱신한다.
    function loadSelectionOnly(a, idParamName) {
        const url = new URL(a.href, location.href);
        const newId = url.searchParams.get(idParamName);
        const list = a.closest('#main_select_right2');
        const banner = content.querySelector('#span_banner');
        if (!newId || !list) {
            loadContent(a.href, true);
            return;
        }

        const previousSelected = list.querySelector('a.selected');
        if (previousSelected) {
            previousSelected.classList.remove('selected');
        }
        a.classList.add('selected');
        if (banner) {
            banner.textContent = a.textContent.trim();
        }
        syncParam(idParamName, newId, ['#main_select_right2']);
        swapScheduleList(a.href);
    }

    content.addEventListener('click', function (e) {
        const a = e.target.closest('a');
        if (!isFilterLink(a)) {
            return;
        }
        e.preventDefault();
        if (a.closest('#dateTrack')) {
            loadDateOnly(a);
            return;
        }
        if (a.closest('#main_select_right2')) {
            const url = new URL(a.href, location.href);
            if (url.searchParams.get('movieId')) {
                loadSelectionOnly(a, 'movieId');
                return;
            }
            if (url.searchParams.get('theaterId')) {
                loadSelectionOnly(a, 'theaterId');
                return;
            }
        }
        loadContent(a.href, true);
    });

    // 좌석 선택으로 넘어가는 링크는 로그인 여부부터 확인한다. 로그인이 안 되어 있으면
    // 로그인 모달(fragment/loginModal.html, js/loginModal.js)을 띄우고, 성공해야 이동한다.
    content.addEventListener('click', function (e) {
        const a = e.target.closest('a');
        if (!a || a.href.indexOf('/movieReservation/seat') === -1) {
            return;
        }
        e.preventDefault();
        const href = a.href;
        requireLogin(function () {
            window.location.href = href;
        });
    });

    window.addEventListener('popstate', function () {
        loadContent(location.href, false);
    });

    if (!history.state || !history.state.movie24ScheduleUrl) {
        history.replaceState({ movie24ScheduleUrl: location.href }, '', location.href);
    }

    initDateScroller();
    initCalendarModal();
});
