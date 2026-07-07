document.querySelectorAll(".popularPoster").forEach(function(item,index) {
    item.addEventListener("mouseenter", () => {
// 					item.nextElementSilbing.style.display = "block";
        document.querySelector("#lookDetail"+(index+1)).style.display="block";
        item.style.opacity = 0.4;
        item.style.border = "2px solid #999797";
    })
});
document.querySelectorAll(".popularPoster").forEach(function(item, index) {
    item.addEventListener("mouseleave", () => {
        document.querySelector("#lookDetail"+(index+1)).style.display="none";
        item.style.opacity = 1;
        item.style.border = "none";
    })
});
document.querySelectorAll(".lookDetail").forEach(function(item,index) {
    item.addEventListener("mouseenter", () => {
        item.style.display="block";
        document.getElementById('mainImg'+(index+1)).style.opacity = 0.4;
        document.getElementById('mainImg'+(index+1)).style.border = "3px solid #999797";
    })
});

var video1 = document.querySelector("#trailer-video1");
var video2 = document.querySelector("#trailer-video2");

const showTrailer = () =>{
    video2.style.display = "none";
}
showTrailer();

function playPause() {
    if (video1.paused) {
        video1.play();
    } else {
        video1.pause();
    }
    if (video2.paused) {
        video2.play();
    } else {
        video2.pause();
    }
}

function nextVideo(){

    if(video1.style.display == "none"){
        video1.style.display = "";
        video2.style.display = "none";
        document.querySelector("#h1-1").style.display = "block";
        document.querySelector("#h2-1").style.display = "block";
        document.querySelector("#h1-2").style.display = "none";
        document.querySelector("#h2-2").style.display = "none";
    }
    else if(video2.style.display == "none"){
        video1.style.display = "none";
        video2.style.display = "";
        document.querySelector("#h1-1").style.display = "none";
        document.querySelector("#h2-1").style.display = "none";
        document.querySelector("#h1-2").style.display = "block";
        document.querySelector("#h2-2").style.display = "block";
    }
};

var p1 = document.getElementsByClassName("popular");
var p2 = document.getElementsByClassName("popular2");
function showPopular(){
    document.querySelector(".popularBtn").style.fontWeight = "900";
    document.querySelector(".popularBtn").style.textDecoration = "underline";
    document.querySelector(".trailerMovieBtn").style.fontWeight = "500";
    document.querySelector(".trailerMovieBtn").style.textDecoration = "none";
    document.querySelector(".popular2").style.display = "none";
    document.querySelector(".popular").style.display = "block";
};
function showComing(){
    document.querySelector(".trailerMovieBtn").style.fontWeight = "900";
    document.querySelector(".trailerMovieBtn").style.textDecoration = "underline";
    document.querySelector(".popularBtn").style.fontWeight = "500";
    document.querySelector(".popularBtn").style.textDecoration = "none";
    document.querySelector(".popular").style.display = "none";
    document.querySelector(".popular2").style.display = "block";
};