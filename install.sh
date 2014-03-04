#!/bin/sh

export BUILD_DIR=/usr/src/alena
export LOGGING_DIR=/var/log/alena
export ALENA_HOME=/opt/alena
export CURRENT_DIR=`pwd`

export PKG_CONFIG_PATH=/usr/local/lib/pkgconfig
export LD_LIBRARY_PATH=/lib:/usr/lib:/usr/local/lib

# FIXME:
#if [ $(cat /etc/environment | grep ALENA_HOME) != "" ]; then
#	echo $ALENA_HOME >> /etc/environment
#fi

create_user_if_not_exist()
{
   awk -F":" '{ print $1 }' /etc/passwd | grep -x $1 > /dev/null
   if [ $? != 0 ]; then
      adduser -M $1
   fi
}

create_directory_if_not_exist()
{
	if [ ! -d "$1" ]; then
		mkdir $1
	fi
}


INSTALL_DEPS=0

while test $# -gt 0; do
    case "$1" in
        -h|--help)
            echo "-h, --help                show help"
            echo "-i, --install-deps"
            exit 0
            ;;
        -i|--install-dependencies)
            if test $# -gt 0; then
                    INSTALL_DEPS=1
            fi
            shift
            ;;
        *)
            break
                ;;
    esac
done


if [ 1 == "$INSTALL_DEPS" ]; then

	if [ -d "$ALENA_HOME" ]; then
		echo "Directory $ALENA_HOME exist. Are you sure this installation from scratch?"
		exit 1
	fi

	create_directory_if_not_exist $BUILD_DIR

	create_user_if_not_exist redis
	create_user_if_not_exist nginx

	yum -y groupinstall "Development tools"
	yum -y install man wget zlib-devel bzip2-devel openssl-devel ncurses-devel sqlite-devel readline-devel tk-devel libffi-devel speex-devel pycairo-devel java-1.7.0-openjdk mc tcpdump

	cd $BUILD_DIR
	
	# download sources
	wget http://python.org/ftp/python/2.7.3/Python-2.7.3.tar.bz2
	wget --no-check-certificate http://pypi.python.org/packages/source/d/distribute/distribute-0.6.27.tar.gz
	wget http://www.tortall.net/projects/yasm/releases/yasm-1.2.0.tar.gz
	wget ftp://ftp.csx.cam.ac.uk/pub/software/programming/pcre/pcre-8.33.zip
	wget http://ftp.gnome.org/pub/gnome/sources/glib/2.38/glib-2.38.2.tar.xz
	wget http://ftp.gnome.org/pub/gnome/sources/gobject-introspection/1.38/gobject-introspection-1.38.0.tar.xz
	wget http://cairographics.org/releases/cairo-1.12.16.tar.xz
	wget http://ftp.gnome.org/pub/GNOME/sources/pygobject/3.10/pygobject-3.10.2.tar.xz
	wget http://gstreamer.freedesktop.org/src/gstreamer/gstreamer-1.2.0.tar.xz
	wget http://gstreamer.freedesktop.org/src/orc/orc-0.4.18.tar.gz
	wget ftp://ftp.videolan.org/pub/videolan/x264/snapshots/last_stable_x264.tar.bz2
	wget --no-check-certificate https://webm.googlecode.com/files/libvpx-v1.2.0.tar.bz2
	wget http://gstreamer.freedesktop.org/src/gst-plugins-base/gst-plugins-base-1.2.0.tar.xz
	wget http://gstreamer.freedesktop.org/src/gst-plugins-good/gst-plugins-good-1.2.0.tar.xz
	wget http://gstreamer.freedesktop.org/src/gst-libav/gst-libav-1.2.0.tar.xz
	wget http://gstreamer.freedesktop.org/src/gst-plugins-ugly/gst-plugins-ugly-1.2.0.tar.xz
	wget http://gstreamer.freedesktop.org/src/gst-plugins-bad/gst-plugins-bad-1.2.0.tar.xz
	wget http://gstreamer.freedesktop.org/src/gst-python/gst-python-1.1.90.tar.gz
	wget ftp://mirrors.dk.telia.net/pub/mirrors/archive.debian.org/debian/pool/main/d/dpkg/dpkg_1.14.31.tar.gz
	wget http://nginx.org/download/nginx-1.3.14.tar.gz
	wget http://nodejs.org/dist/v0.10.22/node-v0.10.22.tar.gz
	wget http://redis.googlecode.com/files/redis-2.6.10.tar.gz

	# unarchive sources
	tar xf Python-2.7.3.tar.bz2
	tar xf distribute-0.6.27.tar.gz
	tar xzf yasm-1.2.0.tar.gz 
	unzip pcre-8.33.zip
	tar xJf glib-2.38.2.tar.xz
	tar xJf gobject-introspection-1.38.0.tar.xz
	tar xJf cairo-1.12.16.tar.xz
	tar xJf pygobject-3.10.2.tar.xz
	tar xJf gstreamer-1.2.0.tar.xz 
	tar xzf orc-0.4.18.tar.gz 
	tar xjf last_stable_x264.tar.bz2
	tar xjf libvpx-v1.2.0.tar.bz2
	tar xJf gst-plugins-base-1.2.0.tar.xz 
	tar xJf gst-plugins-good-1.2.0.tar.xz
	tar xJf gst-libav-1.2.0.tar.xz
	tar xJf gst-plugins-ugly-1.2.0.tar.xz
	tar xJf gst-plugins-bad-1.2.0.tar.xz 
	tar xzf gst-python-1.1.90.tar.gz 
	tar xfz dpkg_1.14.31.tar.gz
	tar xzf nginx-1.3.14.tar.gz
	tar xzf node-v0.10.22.tar.gz
	tar xzf redis-2.6.10.tar.gz

	# Python 2.7
	cd $BUILD_DIR/Python-2.7.3
	./configure
	make && make install

	# update easy_install
	cd $BUILD_DIR/distribute-0.6.27
	python setup.py install

	# install modules for python
	easy_install typecheck
	easy_install redis

	# install yasm >= 1.2.0
	cd $BUILD_DIR/yasm-1.2.0
	./configure
	make && make install

	# install libpcre >= 8.13
	cd $BUILD_DIR/pcre-8.33
	./configure
	make && make install

	# install glib >= 2.32
	cd $BUILD_DIR/glib-2.38.2
	./configure
	make && make install

	cd $BUILD_DIR/gobject-introspection-1.38.0
	./configure --disable-static
	make && make install

	cd $BUILD_DIR/cairo-1.12.16
	./configure --disable-static --enable-xlib=no
	make && make install

	cd $BUILD_DIR/pygobject-3.10.2
	./configure
	make && make install

	# install gstreamer
	cd $BUILD_DIR/gstreamer-1.2.0
	./configure --enable-introspection=yes
	make && make install

	# install orc >= 0.4.18
	cd $BUILD_DIR/orc-0.4.18
	./configure
	make && make install

	# install x264 >= 0.120
	cd $BUILD_DIR
	mv x264-snapshot-* x264-snapshot
	cd x264-snapshot
	./configure --enable-shared --enable-pic
	make && make install

	# install vpx
	cd $BUILD_DIR/libvpx-v1.2.0
	./configure --enable-realtime-only --enable-error-concealment --disable-examples --enable-vp8 --enable-shared --enable-pic --as=yasm
	make && make install

	cd $BUILD_DIR/gst-plugins-base-1.2.0
	./configure --enable-introspection=yes
	make && make install

	cd $BUILD_DIR/gst-plugins-good-1.2.0
	cp $CURRENT_DIR/gst-speex-flvmux.patch $BUILD_DIR/gst-plugins-good-1.2.0
	patch -p1 < gst-speex-flvmux.patch
	./configure
	make && make install

	cd $BUILD_DIR/gst-libav-1.2.0
	./configure
	make && make install

	cd $BUILD_DIR/gst-plugins-ugly-1.2.0
	./configure
	make && make install

	cd $BUILD_DIR/gst-plugins-bad-1.2.0
	./configure --enable-introspection=yes
	make && make install

	cd $BUILD_DIR/gst-python-1.1.90
	./configure
	make && make install

	# You can test success using command below 
	# python -c "from gi.repository import Gst, GObject"

	# install start-stop-daemon
	cd $BUILD_DIR/dpkg-1.14.31
	./configure
	make && cd utils && make install

	ln -s /usr/local/lib/libpcre.so.1 /lib64
	cd $BUILD_DIR/nginx-1.3.14
	./configure --user=nginx --group=nginx --pid-path=/var/run/nginx.pid --sbin-path=/usr/local/nginx/nginx --conf-path=/etc/nginx/nginx.conf --pid-path=/var/run/nginx.pid --with-http_ssl_module --with-pcre
	make && make install
 
	cd $BUILD_DIR/node-v0.10.22
	./configure 
	make && make install 

	cd $BUILD_DIR/redis-2.6.10
	make && make install 
	# prepare config
	cp redis.conf redis.conf.orig
	sed -c -i "s/daemonize no/daemonize yes/" redis.conf
	sed -c -i "s/# bind/bind/" redis.conf
	sed -c -i "s/logfile stdout/logfile \/var\/log\/redis.log/" redis.conf
	sed -c -i "s/dir .\//dir \/opt\/redis/" redis.conf
	cp redis.conf /etc/redis.conf
	# create directory for redis.rdb file
	mkdir -p /opt/redis
	chown redis:redis /opt/redis
fi



if [ -f /etc/init.d/media-controller ]; then
	service media-controller stop
fi

if [ -f /etc/init.d/client-handler ]; then
	service client-handler stop
fi

if [ -f /etc/init.d/webcall-app ]; then
	service webcall-app stop
fi

service nginx stop
service redis stop

cd $CURRENT_DIR

# create old installation backup if exists
if [ -d "$ALENA_HOME" ]; then
	cp -r $ALENA_HOME $CURRENT_DIR/backup
fi

create_user_if_not_exist alena

create_directory_if_not_exist $ALENA_HOME
create_directory_if_not_exist $LOGGING_DIR
chown alena:alena $LOGGING_DIR

chmod +x init.d/*
cp init.d/* /etc/init.d
cp nginx.conf /etc/nginx/nginx.conf

# build stun-agent
cd media-controller/agents
chmod +x build.sh
./build.sh 
cd $CURRENT_DIR

chmod +x media-controller.sh
cp media-controller.sh $ALENA_HOME
cp -r media-controller $ALENA_HOME

chmod +x client-handler.sh
cp client-handler.sh $ALENA_HOME
cp -r client-handler $ALENA_HOME

chmod +x webcall-app.sh
cp webcall-app.sh $ALENA_HOME
cp -r webcall-app $ALENA_HOME

mkdir $ALENA_HOME/www
# TODO: put static here
echo "Men at work" > $ALENA_HOME/www/index.html

# if backup directory exists - copy old configs into new installation
if [ -d $CURRENT_DIR/backup ]; then
	cp  $CURRENT_DIR/backup/media-controller/config.ini $ALENA_HOME/media-controller/config.ini
	cp  $CURRENT_DIR/backup/client-handler/config.ini $ALENA_HOME/client-handler/config.ini
	cp  $CURRENT_DIR/backup/webcall-app/config.ini $ALENA_HOME/webcall-app/config.ini
fi

chown -R alena:alena $ALENA_HOME

# add services to autostart
/sbin/chkconfig --add nginx
/sbin/chkconfig nginx on

/sbin/chkconfig --add redis
/sbin/chkconfig redis on

/sbin/chkconfig --add media-controller
/sbin/chkconfig media-controller on

/sbin/chkconfig --add client-handler
/sbin/chkconfig client-handler on

/sbin/chkconfig --add webcall-app
/sbin/chkconfig webcall-app on


# gracefully start services
service redis start
service nginx start
service media-controller start
service client-handler start
service webcall-app start