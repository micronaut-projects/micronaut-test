-- Try to add 2 products with random id's so they don't conflict with the previous 2 products.
insert into products(id, code, name) values(floor(random() * 1000000000), 'p103', 'Apple Studio') ON CONFLICT DO NOTHING;
insert into products(id, code, name) values(floor(random() * 1000000000), 'p104', 'Samsung TV') ON CONFLICT DO NOTHING;
