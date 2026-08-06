(function () {
    var DEFAULT_CENTER = {lat: 37.5665, lng: 126.9780}; // 서울시청 (위치 정보 없을 때 기본값)

    var map = null;
    var mapReady = false;
    var allTheaters = [];
    var currentSet = [];
    var markers = []; // {theater, marker}
    var userMarker = null;
    var userPosition = null; // {lat, lng}
    var activeOverlay = null;
    var activeListItemEl = null;

    var listEl = document.getElementById('theaterList');
    var statusEl = document.getElementById('theaterLocationStatus');
    var searchInput = document.getElementById('theaterSearchInput');

    document.addEventListener('DOMContentLoaded', function () {
        wireControls();
        loadTheaters();
        requestUserLocation();

        if (KAKAO_MAP_READY) {
            kakao.maps.load(initMap);
        } else {
            statusEl.textContent = '카카오맵 설정이 완료되지 않아 지도 없이 목록만 표시합니다.';
        }
    });

    function wireControls() {
        document.getElementById('theaterSearchBtn').addEventListener('click', runSearch);
        document.getElementById('theaterSearchResetBtn').addEventListener('click', resetSearch);
        document.getElementById('locateMeBtn').addEventListener('click', requestUserLocation);
        searchInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                runSearch();
            }
        });
    }

    function initMap() {
        var container = document.getElementById('theaterMap');
        map = new kakao.maps.Map(container, {
            center: new kakao.maps.LatLng(DEFAULT_CENTER.lat, DEFAULT_CENTER.lng),
            level: 7
        });
        map.addControl(new kakao.maps.ZoomControl(), kakao.maps.ControlPosition.RIGHT);
        mapReady = true;

        if (currentSet.length > 0) {
            renderMarkers(currentSet);
        }
        if (userPosition) {
            placeUserMarker(userPosition);
            map.setCenter(new kakao.maps.LatLng(userPosition.lat, userPosition.lng));
            map.setLevel(6);
        }
    }

    function loadTheaters() {
        fetch(THEATER_API_URL)
            .then(function (res) {
                return res.ok ? res.json() : [];
            })
            .then(function (data) {
                allTheaters = (data || []).filter(function (t) {
                    return t.latitude != null && t.longitude != null;
                });
                applyTheaterSet(allTheaters);
            })
            .catch(function () {
                statusEl.textContent = '극장 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.';
            });
    }

    function applyTheaterSet(theaters) {
        currentSet = theaters;
        renderList(currentSet);
        if (mapReady) {
            renderMarkers(currentSet);
        }
    }

    function requestUserLocation() {
        if (!navigator.geolocation) {
            statusEl.textContent = '이 브라우저에서는 위치 정보를 사용할 수 없어 서울 중심으로 표시합니다.';
            return;
        }
        statusEl.textContent = '현재 위치를 확인하는 중...';
        navigator.geolocation.getCurrentPosition(
            function (position) {
                userPosition = {lat: position.coords.latitude, lng: position.coords.longitude};
                if (mapReady) {
                    placeUserMarker(userPosition);
                    map.setCenter(new kakao.maps.LatLng(userPosition.lat, userPosition.lng));
                    map.setLevel(6);
                }
                statusEl.textContent = '현재 위치에서 가까운 순서로 보여드려요.';
                renderList(currentSet);
            },
            function () {
                statusEl.textContent = '위치 정보 접근이 거부되어 서울 중심으로 표시합니다.';
            },
            {enableHighAccuracy: true, timeout: 8000}
        );
    }

    function placeUserMarker(pos) {
        if (userMarker) {
            userMarker.setPosition(new kakao.maps.LatLng(pos.lat, pos.lng));
            return;
        }
        var svg = '<svg xmlns="http://www.w3.org/2000/svg" width="26" height="26">'
            + '<circle cx="13" cy="13" r="9" fill="#362FD9" stroke="white" stroke-width="3"/></svg>';
        var image = new kakao.maps.MarkerImage(
            'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(svg),
            new kakao.maps.Size(26, 26),
            {offset: new kakao.maps.Point(13, 13)}
        );
        userMarker = new kakao.maps.Marker({
            position: new kakao.maps.LatLng(pos.lat, pos.lng),
            image: image,
            zIndex: 10
        });
        userMarker.setMap(map);
    }

    function renderMarkers(theaters) {
        if (!mapReady) {
            return;
        }
        markers.forEach(function (m) {
            m.marker.setMap(null);
        });
        markers = [];

        theaters.forEach(function (theater) {
            var marker = new kakao.maps.Marker({
                position: new kakao.maps.LatLng(theater.latitude, theater.longitude),
                map: map
            });
            kakao.maps.event.addListener(marker, 'click', function () {
                selectTheater(theater, marker);
            });
            markers.push({theater: theater, marker: marker});
        });

        if (theaters.length > 0) {
            var bounds = new kakao.maps.LatLngBounds();
            theaters.forEach(function (t) {
                bounds.extend(new kakao.maps.LatLng(t.latitude, t.longitude));
            });
            if (userPosition) {
                bounds.extend(new kakao.maps.LatLng(userPosition.lat, userPosition.lng));
            }
            map.setBounds(bounds);
        }
    }

    function selectTheater(theater, marker) {
        if (!mapReady) {
            return;
        }
        if (activeOverlay) {
            activeOverlay.setMap(null);
            activeOverlay = null;
        }
        if (activeListItemEl) {
            activeListItemEl.classList.remove('active');
        }

        map.panTo(marker.getPosition());

        var overlayEl = buildOverlayContent(theater);
        activeOverlay = new kakao.maps.CustomOverlay({
            position: marker.getPosition(),
            content: overlayEl,
            yAnchor: 1.4,
            zIndex: 20
        });
        activeOverlay.setMap(map);

        var listItemEl = listEl.querySelector('[data-theater-id="' + theater.id + '"]');
        if (listItemEl) {
            listItemEl.classList.add('active');
            listItemEl.scrollIntoView({block: 'nearest', behavior: 'smooth'});
            activeListItemEl = listItemEl;
        }
    }

    function buildOverlayContent(theater) {
        var wrap = document.createElement('div');
        wrap.className = 'theater-overlay';

        var closeBtn = document.createElement('button');
        closeBtn.type = 'button';
        closeBtn.className = 'theater-overlay-close';
        closeBtn.textContent = '×';
        closeBtn.addEventListener('click', function () {
            activeOverlay.setMap(null);
            activeOverlay = null;
        });

        var title = document.createElement('h4');
        title.textContent = theater.name;

        var address = document.createElement('p');
        address.className = 'theater-overlay-address';
        address.textContent = theater.address || theater.region;

        var linkRow = document.createElement('div');
        linkRow.className = 'theater-overlay-links';

        var directionsLink = document.createElement('a');
        directionsLink.href = 'https://map.kakao.com/link/to/' + encodeURIComponent(theater.name) + ',' + theater.latitude + ',' + theater.longitude;
        directionsLink.target = '_blank';
        directionsLink.rel = 'noopener noreferrer';
        directionsLink.textContent = '길찾기';

        linkRow.appendChild(directionsLink);

        if (userPosition) {
            var distanceEl = document.createElement('span');
            distanceEl.className = 'theater-overlay-distance';
            distanceEl.textContent = formatDistance(distanceKm(userPosition, theater));
            linkRow.appendChild(distanceEl);
        }

        wrap.appendChild(closeBtn);
        wrap.appendChild(title);
        wrap.appendChild(address);
        wrap.appendChild(linkRow);
        return wrap;
    }

    function renderList(theaters) {
        var withDistance = theaters.map(function (t) {
            return {theater: t, distance: userPosition ? distanceKm(userPosition, t) : null};
        });

        withDistance.sort(function (a, b) {
            if (a.distance == null || b.distance == null) {
                return a.theater.name.localeCompare(b.theater.name);
            }
            return a.distance - b.distance;
        });

        listEl.innerHTML = '';

        if (withDistance.length === 0) {
            var empty = document.createElement('li');
            empty.className = 'theater-list-empty';
            empty.textContent = '검색 결과가 없습니다.';
            listEl.appendChild(empty);
            return;
        }

        withDistance.forEach(function (entry) {
            var theater = entry.theater;
            var li = document.createElement('li');
            li.className = 'theater-list-item';
            li.setAttribute('data-theater-id', theater.id);

            var name = document.createElement('p');
            name.className = 'theater-list-name';
            name.textContent = theater.name;

            var addr = document.createElement('p');
            addr.className = 'theater-list-address';
            addr.textContent = theater.address || theater.region;

            li.appendChild(name);
            li.appendChild(addr);

            if (entry.distance != null) {
                var distanceBadge = document.createElement('span');
                distanceBadge.className = 'theater-list-distance';
                distanceBadge.textContent = formatDistance(entry.distance);
                li.appendChild(distanceBadge);
            }

            li.addEventListener('click', function () {
                if (!mapReady) {
                    return;
                }
                var found = markers.find(function (m) {
                    return m.theater.id === theater.id;
                });
                if (found) {
                    selectTheater(theater, found.marker);
                }
            });

            listEl.appendChild(li);
        });
    }

    function runSearch() {
        var query = searchInput.value.trim().toLowerCase();
        if (!query) {
            resetSearch();
            return;
        }
        var filtered = allTheaters.filter(function (t) {
            return (t.name && t.name.toLowerCase().indexOf(query) !== -1)
                || (t.region && t.region.toLowerCase().indexOf(query) !== -1)
                || (t.address && t.address.toLowerCase().indexOf(query) !== -1);
        });
        applyTheaterSet(filtered);
    }

    function resetSearch() {
        searchInput.value = '';
        applyTheaterSet(allTheaters);
    }

    function distanceKm(a, b) {
        var bLat = b.latitude != null ? b.latitude : b.lat;
        var bLng = b.longitude != null ? b.longitude : b.lng;
        return haversine(a.lat, a.lng, bLat, bLng);
    }

    function haversine(lat1, lng1, lat2, lng2) {
        var R = 6371;
        var dLat = ((lat2 - lat1) * Math.PI) / 180;
        var dLng = ((lng2 - lng1) * Math.PI) / 180;
        var sinLat = Math.sin(dLat / 2);
        var sinLng = Math.sin(dLng / 2);
        var h = sinLat * sinLat
            + Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) * sinLng * sinLng;
        return R * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }

    function formatDistance(km) {
        if (km < 1) {
            return Math.round(km * 1000) + 'm';
        }
        return km.toFixed(1) + 'km';
    }
})();
