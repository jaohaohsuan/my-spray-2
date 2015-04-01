#Resource Access Control
a hypermedia style web api example

不在User与Resource上纪录任权限相关内容，而是使用ResourceKeeper来记录

##ResourceRepository
只有ResourceKeeper能访问

采用单一阶层写入Resource，每个Resource的父亲都是一样的，不使用树状结构

记录格式如下

路径 @ 结构描述 @ 内容

路径: /grandys/rd1/henry
     /customers    

结构描述: /company/department/staff
         /index/type
         
example 1

/grandys/rd1/henry@/company/department/staff@任意内容

example 2

/customers@/index/type@任意内容

##User
如果需要Resource内容时，User不直接对Resource访问，而是透过ResourceKeeper

##Resourcekeeper
只有KeeperOfficeer可以做写入操作

访问权限纪录方式

usr: "bill", value: "grandsys", map: "/campany"

usr: "san", value: "grandys/rd1", map: "/company/department"

usr: "henry", value: "customers", map: "/index"

##KeeperOfficeer
处理User的Resource存取申请


##add new resource + add permission flow

msg "bill@grandsys@/campany@It's focused on call center industry since 1990.@_@_@_"

sender is user / keeperOfficer ! "bill@grandsys@/campany@_@passed@_@_"

sender is keeperOfficer / resourceKeepr ! "bill@grandsys@/campany@_@_@allowed@_"

sender is resourceKeepr / resourceRepository ! "_@grandsys@/campany@It's focused on call center industry since 1990.@_@_@located"

<<< "bill@grandsys@/campany@It's focused on call center industry since 1990.@passed@allowed@located"

##add new resource flow

msg "bill@grandsys/rd1@/campany/department@san is the manager.@_@_"

sender is user / resourceKeepr ! "bill@grandsys/rd1@/campany/department@_@_@_" if allowed

sender is resourceKeepr / resourceRepository ! "_@grandsys@/campany@san is the manager.@allowed@_"

<<< "bill@grandsys/rd1@/campany/department@san is the manager.@allowed@located"


##get resource flow

msg "bill@grandsys@/campany@_"

sender is user / resourceKeeper ! "bill@grandsys@/campany@_"

sender is resourceKeeper / resourceRepository ! "_@grandsys@/campany@_"

<<< "bill@grandsys@/campany@It's focused on call center industry since 1990."


