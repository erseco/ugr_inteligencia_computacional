#!/usr/bin/env python
'''
--------------------------------------------------------------------------------
IC - Inteligencia Computacional
Master en Ingeniería Informática (UGR)
2017 - Ernesto Serrano <erseco@correo.ugr.es>
--------------------------------------------------------------------------------
'''
from __future__ import print_function
import keras
from keras.datasets import mnist
from keras.models import Sequential
from keras.layers import Dense, Dropout, Flatten, Activation
from keras.layers import Conv2D, MaxPooling2D
from keras import backend as K
import keras.callbacks as cb
import numpy as np
from keras.utils import plot_model
import time
from matplotlib import pyplot as plt
plt.switch_backend('agg')

class LossHistory(cb.Callback):
    def on_train_begin(self, logs={}):
        self.losses = []

    def on_batch_end(self, batch, logs={}):
        batch_loss = logs.get('loss')
        self.losses.append(batch_loss)


batch_size = 128
num_classes = 10
epochs = 30

# input image dimensions
img_rows, img_cols = 28, 28

# the data, shuffled and split between train and test sets
(x_train, y_train), (x_test, y_test) = mnist.load_data()

if K.image_data_format() == 'channels_first':
    x_train = x_train.reshape(x_train.shape[0], 1, img_rows, img_cols)
    x_test = x_test.reshape(x_test.shape[0], 1, img_rows, img_cols)
    input_shape = (1, img_rows, img_cols)
else:
    x_train = x_train.reshape(x_train.shape[0], img_rows, img_cols, 1)
    x_test = x_test.reshape(x_test.shape[0], img_rows, img_cols, 1)
    input_shape = (img_rows, img_cols, 1)

x_train = x_train.astype('float32')
x_test = x_test.astype('float32')
x_train /= 255
x_test /= 255
print('x_train shape:', x_train.shape)
print(x_train.shape[0], 'train samples')
print(x_test.shape[0], 'test samples')

# convert class vectors to binary class matrices
y_train = keras.utils.to_categorical(y_train, num_classes)
y_test = keras.utils.to_categorical(y_test, num_classes)

model = Sequential()

model.add(Conv2D(32, kernel_size=(5, 5),
                 activation='relu',
                 input_shape=input_shape))

model.add(MaxPooling2D(pool_size=(2, 2)))
model.add(Conv2D(64, (3, 3), activation='relu'))
model.add(MaxPooling2D(pool_size=(2, 2)))
model.add(Dropout(0.2))
model.add(Flatten())
model.add(Dense(512, activation='relu'))
model.add(Dense(128, activation='relu'))
model.add(Dense(50, activation='relu'))
model.add(Dropout(0.5))
model.add(Dense(num_classes, activation='softmax'))

model.summary()


model.compile(loss=keras.losses.categorical_crossentropy,
              optimizer=keras.optimizers.Adadelta(),
              metrics=['accuracy'])

history = LossHistory()


start = time.time()

model.fit(x_train, y_train,
          batch_size=batch_size,
          callbacks=[history],
          epochs=epochs,
          verbose=1,
          validation_data=(x_test, y_test))


end = time.time()
print("training time:")
print(end - start)

score = model.evaluate(x_train, y_train, verbose=0)
print('Train loss:', score[0]*100, '%')
print('Train accuracy:', score[1]*100, '%')

score = model.evaluate(x_test, y_test, verbose=0)
print('Test loss:', score[0]*100, '%')
print('Test accuracy:', score[1]*100, '%')


result = model.predict(x_test)

class_result=np.argmax(result,axis=-1)

print(class_result)

np.savetxt('test.txt', class_result.astype('int'), delimiter="")
plot_model(model, to_file='model.png')


plt.switch_backend('agg')
plt.ioff()
fig = plt.figure()

ax = fig.add_subplot(111)
ax.plot(history.losses)
ax.set_title('Perdida por lote')

fig.savefig('plot.png')
