test = importdata('1367449744.test.csv');
test = test.data;
labels = test(1:2:11, 2);

mat = reshape(test(1:55, 5), 11, 5);
subplot(1,4,1),plot(0:100:1000, mat)
axis([0,1000,0,1])
set(gca,'XTick',0:200:1000), set(gca,'XTickLabel',labels)
title('(a) Topic Independent')
legend('Test-0','Test-.25','Test-.5','Test-.75','Test-1', 'Location','SouthEastOutside','Orientation','horizontal')

mat = reshape(test(56:110, 5), 11, 5);
subplot(1,4,2),plot(0:100:1000, mat)
axis([0,1000,0,1])
set(gca,'XTick',0:200:1000), set(gca,'XTickLabel',labels)
title('(b) Topic Independent & Expanded Negative')

mat = reshape(test(111:165, 5), 11, 5);
subplot(1,4,3),plot(0:100:1000, mat)
axis([0,1000,0,1])
set(gca,'XTick',0:200:1000), set(gca,'XTickLabel',labels)
title('(c) Topic Specific')

mat = reshape(test(166:220, 5), 11, 5);
subplot(1,4,4),plot(0:100:1000, mat)
axis([0,1000,0,1])
set(gca,'XTick',0:200:1000), set(gca,'XTickLabel',labels)
title('(d) Topic Specific & Expanded Negative')
