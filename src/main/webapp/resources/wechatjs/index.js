'use strict';


let htmlWidth = document.documentElement.clientWidth || document.body.clientWidth;
let htmlDom = document.getElementsByTagName('html')[0];
// htmlDom.style.fontSize = htmlWidth / 10 + 'px';
if (htmlWidth>450 && htmlWidth<=500) {
	htmlDom.style.fontSize = htmlWidth / 15 + 'px';
}else if(htmlWidth>500){
	htmlDom.style.fontSize = htmlWidth / 19.4 + 'px';
}else{
	htmlDom.style.fontSize = htmlWidth / 10 + 'px';
}

window.addEventListener('resize', (e) => {
	let htmlWidth = document.documentElement.clientWidth || document.body.clientWidth;
	htmlDom.style.fontSize = htmlWidth / 10 + 'px';
})