## Installation

To install MySQL from the operating system distribution:

```
$ dnf install community-mysql-server
```

To install MySQL from upstream distribution:

```
$ cat > /etc/yum.repos.d/mysql-community.repo << EOF
[mysql-5.7-community]
name=MySQL 5.7 Community Server
baseurl=http://repo.mysql.com/yum/mysql-5.7-community/fc/$releasever/$basearch/
enabled=1
gpgcheck=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-mysql
EOF
```

Alternatively, download the files directly from [upstream repository](http://repo.mysql.com/yum/mysql-5.7-community/fc/31/x86_64/):
```
$ dnf localinstall *.rpm
```

## Starting MySQL Server

To start the server:

```
$ systemctl start mysqld.service
```

It will create the initial files in /var/lib/mysql and store the logs in /var/log/mysqld.log.

## Enabling MySQL Service

```
$ systemctl enable mysqld.service
```

## See Also

* [How To Install MySQL 8.0 on Fedora 30/29/28](https://tecadmin.net/install-mysql-8-on-fedora/)
* [InnoDB Backup](https://dev.mysql.com/doc/refman/8.0/en/innodb-backup.html)
* [InnoDB Recovery](https://dev.mysql.com/doc/refman/8.0/en/innodb-recovery.html)
* [Forcing InnoDB Recovery](https://dev.mysql.com/doc/refman/5.7/en/forcing-innodb-recovery.html)
* [Resetting MySQL root password](https://stackoverflow.com/questions/41645309/mysql-error-access-denied-for-user-rootlocalhost)