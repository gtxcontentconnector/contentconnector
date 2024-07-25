FROM docker.apa-it.at/maven:3.8.8-amazoncorretto-17-debian-bookworm
MAINTAINER Bernhard Kaszt <b.kaszt@gentics.com>

ARG GIT_USER_NAME="Maven Release Manager"
ARG GIT_USER_EMAIL="entwicklung@gentics.com"
ARG USER_NAME="jenkins"

COPY build/sources.list /etc/apt/
RUN apt-get update \
	&& apt-get install --assume-yes \
		lsb-release \
		wget \
		curl \
		apt-transport-https \
		ca-certificates \
		gnupg2 \
		software-properties-common \
		dirmngr \
	&& apt-get install --assume-yes \
		doxygen \
		git-core \
		sudo \
		less \
		vim \
		tar \
		psmisc \
		locales \
		build-essential \
		devscripts \
		zip \
		unzip \
		nmap \
		bzip2 \
		procps

# Install docker-compose
RUN sudo curl -L "https://github.com/docker/compose/releases/download/1.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose \
	&& sudo chmod +x /usr/local/bin/docker-compose

ENV JAVA_HOME /usr/lib/jvm/java-17-amazon-corretto

# Set the locale (needed for ruby guides)
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
	locale-gen
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# Install Ruby 2.3 with RVM
ENV PATH "/usr/local/rvm/bin:$PATH"
ENV rvm_path "/usr/local/rvm"
RUN curl -sSL https://rvm.io/mpapis.asc | gpg2 --import - \
 && curl -sSL https://rvm.io/pkuczynski.asc | gpg2 --import - \
 && curl -sSL https://get.rvm.io | bash \
 && rvm pkg install openssl \
 && rvm install --autolibs=disable 2.3 -C --with-openssl-dir=$rvm_path/usr

# Install Guides
ADD ./build/guides-wrapper.sh /usr/local/bin/guides
RUN rvm-exec 2.3 gem install guides -- --with-cflags="-O2 -pipe -march=native -w" \
 &&	chmod +x /usr/local/bin/guides

# Add user
# The ID / GID 10000 is used by the jenkinsci/slave Dockerfile and has to match here, because Jenkins
# does the SCM checkout in the jnlp container for some reasons.
RUN groupadd --system --gid 10000 ${USER_NAME} && useradd --create-home --no-log-init --uid 10000 --gid ${USER_NAME} ${USER_NAME}

# Setup Git
RUN git config --system user.name "${GIT_USER_NAME}"
RUN git config --system user.email "${GIT_USER_EMAIL}"

RUN echo "StrictHostKeyChecking no" > /etc/ssh/ssh_config
RUN echo "UserKnownHostsFile=/dev/null" >> /etc/ssh/ssh_config
RUN echo "BatchMode yes" >> /etc/ssh/ssh_config
RUN mv /usr/share/maven/conf/settings.xml /usr/share/maven/conf/settings-original.xml
ADD build/settings.xml /usr/share/maven/conf/settings.xml

USER ${USER_NAME}

# Workaround for Maven not outputting colors and silence download progress messages
ENV MAVEN_OPTS "-Djansi.passthrough=true -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"

RUN mkdir -p ~/workspace
RUN mkdir -p ~/.m2/repository
