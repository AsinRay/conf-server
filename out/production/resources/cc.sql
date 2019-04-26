
== 00:08:16.718
insert into pop_up (
    user_id,  
    coin_type,  
    to_addr,  
    quantity,  
    unit,  
    exchange_deposit_id,  
    exchange_update,  
    create_time,  
    status,  
    update_time )
    
values (  
    1334540922272088064,  
    'xrp',  
    'rfTvTCd9GkfxpMS5oksYVjdGZpSpXRgnbt',  
    3,  
    '',  
    'unknown',  
    '2019-04-22 16:06:01',  
    '2019-04-22 16:06:01',  
    10,
    now() 
    );
update balance  set available = available + 3 where user_id =1334540922272088064  and coin = 'xrp';

## 未提 ============


== 00:40:13.389    2.1 更改为正确的用户  

select * from pop_up where coin ='xrp' and quantity=2.1;

update pop_up set user_id=js where coin ='xrp' and quantity=2.1;
update balance set available = available - 2.1 where user_id = old and coin = 'xrp'
update balance set available = available + 2.1 where user_id = js and coin = 'xrp'

------------------------------------------

== 15:05:03.058   2.05   更改为正确的用户 

select * from pop_up where coin_type ='xrp' and quantity=2.05;

update pop_up set user_id=js where coin ='xrp' and quantity=2.1;
update balance set available = available - 2.1 where user_id = old and coin = 'xrp'
update balance set available = available + 2.1 where user_id = js and coin = 'xrp'


== 15:56:08.652  1.311   更改为正确的用户 


== 16:18:47.371  1.121   未入库

insert into pop_up (
    user_id,  
    coin_type,  
    to_addr,  
    quantity,  
    unit,  
    exchange_deposit_id,  
    exchange_update,  
    create_time,  
    status,  
    update_time )
    
values (  
    1334540922272088064,  
    'xrp',  
    'rfTvTCd9GkfxpMS5oksYVjdGZpSpXRgnbt',  
    1.121,  
    '',  
    'unknown',  
    '2019-04-23 16:18:47',  
    '2019-04-23 16:18:47',  
    10,
    now() 
    );
update balance  set available = available + 1.121 where user_id =1334540922272088064  and coin = 'xrp';


== 16:37:51.326  1.33    未入库


insert into pop_up (
    user_id,  
    coin_type,  
    to_addr,  
    quantity,  
    unit,  
    exchange_deposit_id,  
    exchange_update,  
    create_time,  
    status,  
    update_time )
    
values (  
    1334540922272088064,  
    'xrp',  
    'rfTvTCd9GkfxpMS5oksYVjdGZpSpXRgnbt',  
    1.33,  
    '',  
    'unknown',  
    '2019-04-23 16:37:51',  
    '2019-04-23 16:37:51',  
    10,
    now() 
    );
update balance  set available = available + 1.33 where user_id =1334540922272088064  and coin = 'xrp';