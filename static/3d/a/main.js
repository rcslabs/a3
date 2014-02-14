/**
 * Copyright (c) 2009, Benjamin Joffe
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

var map;
var canvas;
var overlay;


var pi=Math.PI;

var total=0;

Number.prototype.range=function(){
	return (this+2*pi)%(2*pi);
}
Number.prototype.roundC=function(){
	return Math.round(this*100)/100;
}

var total=0;

var samples=200;
var W = -1;


var PLAYERS = {};


var arena=[];
arena[0]=[W,W,W,W,W,W,W,W,W,W]
arena[1]=[W,0,0,0,0,0,0,0,0,W]
arena[2]=[W,0,0,0,0,0,0,0,0,W]
arena[3]=[W,0,0,0,0,0,0,0,0,W]
arena[4]=[W,0,0,0,0,0,0,0,0,W]
arena[5]=[W,1,W,W,W,W,W,2,W,W]
arena[6]=[W,1,1,W,2,2,2,2,2,W]
arena[7]=[W,1,1,W,2,2,2,2,2,W]
arena[8]=[W,1,1,W,2,2,2,2,2,W]
arena[9]=[W,W,W,W,W,W,W,W,W,W]


var playerPos=[4,4]; // x,y (from top left)
var playerDir=0.4; // theta, facing right=0=2pi
var playerPosZ=1;
var key=[0,0,0,0,0]; // left, right, up, down

var playerVelY=0;


function getRoom() {
	var  x = Math.floor(playerPos[0]), y = Math.floor(playerPos[1]);
	return arena[x][y]
}


var face=[];

function wallDistance(theta){

	var data=[];
	face = [];

	var x = playerPos[0], y = playerPos[1];
	var deltaX, deltaY;
	var distX, distY;
	var stepX, stepY;
	var mapX, mapY;
	

	var atX=Math.floor(x), atY=Math.floor(y);

	var thisRow=-1;
	var thisSide=-1;

	var lastHeight=0;

	for (var i=0; i<samples; i++) {
		theta+=pi/(3*samples)+2*pi;
		theta%=2*pi;

		mapX = atX, mapY = atY;

		deltaX=1/Math.cos(theta);
		deltaY=1/Math.sin(theta);

		if (deltaX>0) {
			stepX = 1;
			distX = (mapX + 1 - x) * deltaX;
		}
		else {
			stepX = -1;
			distX = (x - mapX) * (deltaX*=-1);		
		}
		if (deltaY>0) {
			stepY = 1;
			distY = (mapY + 1 - y) * deltaY;
		}
		else {
			stepY = -1;
			distY = (y - mapY) * (deltaY*=-1);
		}

		for (var j=0; j<20; j++) {
			if (distX < distY) {
				mapX += stepX;
				if (arena[mapX][mapY] === W) {
					if (thisRow!=mapX || thisSide!=0) {
						if (i>0) {
							data.push(i);
							data.push(lastHeight);
						}
						data.push(i);
						data.push(distX);
						thisSide=0;
						thisRow=mapX;
						face.push(1+stepX);
					}
					lastHeight=distX;
					break;
				}
				distX += deltaX;
			}
			else {
				mapY += stepY;
				if (arena[mapX][mapY] === W) {
					if (thisRow!=mapY || thisSide!=1) {
						if (i>0) {
							data.push(i);
							data.push(lastHeight);
						}	
						data.push(i);
						data.push(distY);
						thisSide=1;
						thisRow=mapY;
						face.push(2+stepY)
					}
					lastHeight=distY;
					break;
				}
				distY += deltaY;
			}
		}
	}
	data.push(i);
	data.push(lastHeight);
	
	return data;
}


function drawCanvas(){

	canvas.clearRect(0,0,400, 300);

	var theta = playerDir-pi/6;

	var wall=wallDistance(theta);

	map.beginPath();
	map.clearRect(0,0,80,80);
	map.fillStyle="#3366CC";
	map.arc(playerPos[0]*8, playerPos[1]*8, 3, 0, 2*pi, true);
	
	map.fill();
	map.beginPath();
	map.moveTo(8*playerPos[0], 8*playerPos[1]);


	var linGrad;
	
	var tl,tr,bl,br;
	
	var theta1,theta2,fix1,fix2;

	for (var i=0; i<wall.length; i+=4) {

		theta1=playerDir-pi/6 + pi*wall[i]/(3*samples);
		theta2=playerDir-pi/6 + pi*wall[i+2]/(3*samples);
		
		fix1 = Math.cos(theta1-playerDir);
		fix2 = Math.cos(theta2-playerDir);

		var h=2-playerPosZ;

		var wallH1=100/(wall[i+1]*fix1);
		var wallH2=100/(wall[i+3]*fix2);

		tl=[wall[i]*2, 150-wallH1*h];
		tr=[wall[i+2]*2, 150-wallH2*h]
		br=[wall[i+2]*2, tr[1]+wallH2*2];
		bl=[wall[i]*2, tl[1]+wallH1*2]

		var shade1=Math.floor(wallH1*2+20); if (shade1>255) shade1=255;
		var shade2=Math.floor(wallH2*2+20); if (shade2>255) shade2=255;

		linGrad = canvas.createLinearGradient(tl[0],0,tr[0],0);
		linGrad.addColorStop(0, 'rgba('+(face[i/4]%2==0 ? shade1 : 0)+','+(face[i/4]==1 ? shade1 : 0)+','+(face[i/4]==2 ? 0 : shade1)+',1.0)');
		linGrad.addColorStop(1, 'rgba('+(face[i/4]%2==0 ? shade2 : 0)+','+(face[i/4]==1 ? shade2 : 0)+','+(face[i/4]==2 ? 0 : shade2)+',1.0)');

		canvas.beginPath();
		canvas.moveTo(tl[0], tl[1]);
		canvas.lineTo(tr[0], tr[1]);
		canvas.lineTo(br[0], br[1]);
		canvas.lineTo(bl[0], bl[1]);
		canvas.fillStyle = linGrad;
		canvas.fill();


		map.lineTo(playerPos[0]*8+Math.cos(theta1)*(wall[i+1])*8, playerPos[1]*8+Math.sin(theta1)*(wall[i+1])*8);
		map.lineTo(playerPos[0]*8+Math.cos(theta2)*(wall[i+3])*8, playerPos[1]*8+Math.sin(theta2)*(wall[i+3])*8);
	}
	canvas.fill();

	canvas.beginPath();
	var x = playerPos[0], y = playerPos[1], a = playerDir;
	if(a > Math.PI) a = a - 2*Math.PI
	console.log("Me at (", x, " ", y, ") ", a )
	canvas.fillStyle="yellow";
	for(pName in PLAYERS) {
		var p = PLAYERS[pName], px = p.x, py = p.y, x = playerPos[0], y = playerPos[1];
		var pa = Math.atan2(py-y, px-x), da = a - pa, dist = Math.sqrt((px-x) * (px-x) + (py-y)*(py-y));
		if(da > Math.PI)  da -= 2*Math.PI;
		if(da < -Math.PI) da += 2*Math.PI;
		console.log("Drawing", pName, px, py, " da=", da, " dist=", dist);
		
		var xx = (-da + 0.5) * 400;
		if(xx >=-30 && xx <= 430) {
			console.log("DRAWING");
			var fullHeightAtDist = 1 / 0.7 * dist;
			console.log("FullHeight At dist =", fullHeightAtDist);
			var r = 0.25 / fullHeightAtDist * 300;
			

			canvas.arc(xx, 150 - r/2, r, 0, 2 * Math.PI, true);
			canvas.fillText(pName, xx, 150 - 2*r - 10);
		}
	}
	canvas.fill();




	map.fillStyle="#FF0000"
	map.fill();
	
	map.fillStyle = "#33CC66";
	for(var pName in PLAYERS) {
		var p = PLAYERS[pName];
		map.moveTo(8*p.x, 8*p.y);
		map.arc(p.x*8, p.y*8, 3, 0, 2*pi, true);
	}
	map.fill();
}

function nearWall(x,y){
	var xx,yy;
	if (isNaN(x)) x=playerPos[0];
	if (isNaN(y)) y=playerPos[1];
	for (var i=-0.1; i<=0.1; i+=0.2) {
		xx=Math.floor(x+i)
		for (var j=-0.1; j<=0.1; j+=0.2) {
			yy=Math.floor(y+j);
			if (arena[xx][yy] === W) return true;
		}
	}
	return false;
}


var jumpCycle=0;



function update(){

	total++;

	var change=false;

	if (jumpCycle) {
		jumpCycle--;
		change=true;
		playerPosZ = 1 + jumpCycle*(20-jumpCycle)/110;
	}
	else if (key[4]) jumpCycle=20;
	
	if (key[0]) {
		if (!key[1]) {
			playerDir-=0.07; //left
			change=true;
		}
	}
	else if (key[1]) {
		playerDir+=0.07; //right
		change=true;
	}

	if (change) {
		playerDir+=2*pi;
		playerDir%=2*pi;
		document.getElementById("sky").style.backgroundPosition=Math.floor(1-playerDir/(2*pi)*2400)+"px 0";
	}

	if (key[2] && !key[3]) {
		if (playerVelY<0.1) playerVelY += 0.02;
	}
	else if (key[3] && !key[2]) {
		if (playerVelY>-0.1) playerVelY -= 0.02;
	}
	else {
		if (playerVelY<-0.02) playerVelY += 0.015;
		else if (playerVelY>0.02) playerVelY -= 0.015;
		else playerVelY=0;
	}
	
	
	if (playerVelY!=0) {

		var oldX=playerPos[0];;
		var oldY=playerPos[1];		
		var newX=oldX+Math.cos(playerDir)*playerVelY;
		var newY=oldY+Math.sin(playerDir)*playerVelY;

		if (!nearWall(newX, oldY)) {
			playerPos[0]=newX;
			oldX=newX;
			change=true;
		}
		if (!nearWall(oldX, newY)) {
			playerPos[1]=newY;
			change=true;
		}

	}
	
	if (change) {
		drawCanvas();
		//posChanged();
	}

}


function changeKey(which, to){
	switch (which){
		case 65: case 37: key[0]=to; break; // left
		case 87: case 38: key[2]=to; break; // up
		case 68: case 39: key[1]=to; break; // right
		case 83: case 40: key[3]=to; break; // down
		case 32: key[4]=to; break; // space bar;
		case 17: key[5]=to; break; // ctrl
	}
}
document.onkeydown=function(e){changeKey((e||window.event).keyCode, 1);}
document.onkeyup=function(e){changeKey((e||window.event).keyCode, 0);}


function initUnderMap(){
	var underMap=document.getElementById("underMap").getContext("2d");
	underMap.fillStyle="#FFF";
	underMap.fillRect(0,0, 200, 200);
	underMap.fillStyle="#444";
	for (var i=0; i<arena.length; i++) {
		for (var j=0; j<arena[i].length; j++) {
			if (arena[i][j] === W) underMap.fillRect(i*8, j*8, 8, 8);
		}	
	}
}


window.onload=function(){
	var ele = document.getElementById("map");
	if (!ele.getContext)
	{
	  alert('An error occured creating a Canvas 2D context. This may be because you are using an old browser, if not please contact me and I\'ll see if I can fix the error.');
	  return;
	}
	map=ele.getContext("2d");
	canvas=document.getElementById("canvas").getContext("2d");
	overlay=document.getElementById("overlay");
	document.getElementById("sky").style.backgroundPosition=Math.floor(-playerDir/(2*pi)*2400)+"px 0";
	drawCanvas();
	initUnderMap();
	setInterval(update, 35);
}



