train = importdata('classification-train.csv');
train = train.data;
test = importdata('classification-test.csv');
test = test.data;
labels = test(1:4:21, 1);

mat = [train(22:42,4), reshape(test(22:126, 4), 21, 5)];
subplot(2,4,1),plot(0:50:1000, mat)
axis([0,1000,0,1])
set(gca,'XTick',0:200:1000), set(gca,'XTickLabel',labels)
title('(a) LibSvm K=0')

mat = [train(43:63,4), reshape(test(127:231, 4), 21, 5)];
subplot(2,4,2),plot(0:50:1000, mat)
axis([0,1000,0,1])
set(gca,'XTick',0:200:1000), set(gca,'XTickLabel',labels)
title('(b) LibSvm K=1')

mat = [train(64:84,4), reshape(test(232:336, 4), 21, 5)];
subplot(2,4,3),plot(0:50:1000, mat)
axis([0,1000,0,1])
set(gca,'XTick',0:200:1000), set(gca,'XTickLabel',labels)
title('(c) LibSvm K=2')

mat = [train(85:105,4), reshape(test(337:441, 4), 21, 5)];
subplot(2,4,4),plot(0:50:1000, mat)
axis([0,1000,0,1])
set(gca,'XTick',0:200:1000), set(gca,'XTickLabel',labels)
title('(d) Logistic (Weka)')

mat = [train(106:126,4), reshape(test(442:546, 4), 21, 5)];
subplot(2,4,5),plot(0:50:1000, mat)
axis([0,1000,0,1])
set(gca,'XTick',0:200:1000), set(gca,'XTickLabel',labels)
title('(e) LogReg (SU)')
legend('Train','Test-0','Test-.25','Test-.5','Test-.75','Test-1', 'Location','SouthEastOutside','Orientation','horizontal')

mat = [train(127:147,4), reshape(test(547:651, 4), 21, 5)];
subplot(2,4,6),plot(0:50:1000, mat)
axis([0,1000,0,1])
set(gca,'XTick',0:200:1000), set(gca,'XTickLabel',labels)
title('(f) LogReg Quad')

mat = [train(148:168,4), reshape(test(652:756, 4), 21, 5)];
subplot(2,4,7),plot(0:50:1000, mat)
axis([0,1000,0,1])
set(gca,'XTick',0:200:1000), set(gca,'XTickLabel',labels)
title('(g) NB')

mat = [train(169:189,4), reshape(test(757:861, 4), 21, 5)];
subplot(2,4,8),plot(0:50:1000, mat)
axis([0,1000,0,1])
set(gca,'XTick',0:200:1000), set(gca,'XTickLabel',labels)
title('(h) NB Quad')
